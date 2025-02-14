/*******************************************************************************
 * Copyright (c) 2020, 2024 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.ibm.ws.io.smallrye.graphql.component;

import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.WeakHashMap;

import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;

import org.eclipse.microprofile.graphql.ConfigKey;
import org.jboss.jandex.IndexView;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;

import com.ibm.websphere.ras.Tr;
import com.ibm.websphere.ras.TraceComponent;
import com.ibm.ws.container.service.app.deploy.ModuleInfo;
import com.ibm.ws.ffdc.annotation.FFDCIgnore;
import com.ibm.ws.io.smallrye.graphql.ui.GraphiQLUIServlet;
import com.ibm.ws.webcontainer.webapp.WebApp;
import com.ibm.wsspi.adaptable.module.NonPersistentCache;
import com.ibm.wsspi.logging.Introspector;

import graphql.schema.GraphQLSchema;
import io.smallrye.graphql.bootstrap.Bootstrap;
import io.smallrye.graphql.cdi.config.GraphQLConfig;
import io.smallrye.graphql.execution.ExecutionService;
import io.smallrye.graphql.execution.SchemaPrinter;
import io.smallrye.graphql.schema.SchemaBuilder;
import io.smallrye.graphql.schema.model.Schema;
import io.smallrye.graphql.servlet.ExecutionServlet;
import io.smallrye.graphql.servlet.IndexInitializer;
import io.smallrye.graphql.servlet.SchemaServlet;

@Component(property = { "service.vendor=IBM" })
public class GraphQLServletContainerInitializer implements ServletContainerInitializer, Introspector {
    private static final TraceComponent tc = Tr.register(GraphQLServletContainerInitializer.class);

    private static Map<ClassLoader, DiagnosticsBag> diagnostics = new WeakHashMap<ClassLoader, DiagnosticsBag>();
    private GraphQLSecurityInitializer securityInitializer;
    public static final String EXECUTION_SERVLET_NAME = "ExecutionServlet";
    public static final String SCHEMA_SERVLET_NAME = "SchemaServlet";
    public static final String UI_SERVLET_NAME = "UIServlet";

    @Reference(cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC, policyOption = ReferencePolicyOption.GREEDY)
    public void setSecurityInitializer(GraphQLSecurityInitializer secInitializer) {
        securityInitializer = secInitializer;
    }

    public void unsetSecurityInitializer(GraphQLSecurityInitializer secInitializer) {
        if (securityInitializer == secInitializer) {
            securityInitializer = null;
        }
    }

    @FFDCIgnore({Throwable.class})
    public void onStartup(Set<Class<?>> classes, ServletContext ctx) throws ServletException {
        if (TraceComponent.isAnyTracingEnabled() && tc.isDebugEnabled()) {
            Tr.entry(tc, "onStartup", new Object[] {classes, ctx, ctx.getServletContextName(), ctx.getContextPath()});
        }

        DiagnosticsBag diagBag = diagnostics.computeIfAbsent(ctx.getClassLoader(), k -> new DiagnosticsBag());
        URL webinfClassesUrl = null;
        try {
            String realPath = ctx.getRealPath("/WEB-INF/classes");
            if (TraceComponent.isAnyTracingEnabled() && tc.isDebugEnabled()) {
                Tr.debug(tc, "realPath: " + realPath);
            }
            if (realPath != null) {
                webinfClassesUrl = Paths.get(realPath).toUri().toURL();
            }
        } catch (MalformedURLException ex) {
            throw new ServletException("Unable to find classes in webapp, " + ctx.getServletContextName(), ex);
        }
        if (webinfClassesUrl == null && ctx instanceof WebApp) {
            WebApp wapp = WebApp.class.cast(ctx);
            try {
                NonPersistentCache overlayCache = wapp.getModuleContainer().adapt(NonPersistentCache.class);
                ModuleInfo moduleInfo = (ModuleInfo) overlayCache.getFromCache(ModuleInfo.class);
                String containerPhysicalPath = moduleInfo.getContainer().getPhysicalPath();
                if (TraceComponent.isAnyTracingEnabled() && tc.isDebugEnabled()) {
                    Tr.debug(tc, "moduleInfo.getContainer().getPhysicalPath() == " + containerPhysicalPath);
                }
                
                if (containerPhysicalPath == null) {
                    // this can occur for "system" apps
                    if (TraceComponent.isAnyTracingEnabled() && tc.isDebugEnabled()) {
                        Tr.debug(tc, "Cannot find app path, will not process for GraphQL APIs");
                    }
                    return;
                }
                Path containerPath = Paths.get(containerPhysicalPath);
                Path webinfClassesPath = containerPath.resolve("WEB-INF").resolve("classes");
                if (Files.exists(webinfClassesPath)) {
                    webinfClassesUrl = webinfClassesPath.toUri().toURL(); // most likely expanded/loose app
                } else {
                    webinfClassesUrl = containerPath.toUri().toURL(); // most likely a JAR/WAR
                }
                
                diagBag.appName = wapp.getApplicationName();
            } catch (Exception ex) {
                if (TraceComponent.isAnyTracingEnabled() && tc.isDebugEnabled()) {
                    Tr.debug(tc, "Failed to find WEB-INF/classes from container in moduleInfo", ex);
                }
            }
        }
        diagBag.webinfClassesUrl = webinfClassesUrl;

        
        GraphQLSchema graphQLSchema = null;
        try {
            IndexInitializer indexInitializer = new IndexInitializer();
            IndexView index = indexInitializer.createIndex(Collections.singleton(webinfClassesUrl));
            Schema schema = SchemaBuilder.build(index);
            if (schema == null || (!schema.hasQueries() && !schema.hasMutations())) {
                // not a GraphQL app as far as we can tell - trace and exit:
                if (TraceComponent.isAnyTracingEnabled() && tc.isDebugEnabled()) {
                    Tr.debug(tc, "No GraphQL components found in app: " + ctx.getServletContextName());
                }
                diagnostics.remove(ctx.getClassLoader());
                return;
            }
            diagBag.modelSchema = schema;
        } catch (Throwable t) {
            Tr.error(tc, "ERROR_GENERATING_SCHEMA_CWMGQ0001E", ctx.getServletContextName());
            throw new ServletException(t);
        }
        GraphQLConfig config = new GraphQLConfig() {
            @Override
            public String getDefaultErrorMessage() {
                return ConfigFacade.getOptionalValue(ConfigKey.DEFAULT_ERROR_MESSAGE, String.class)
                                   .orElse("Server Error");
            }

            @Override
            public boolean isPrintDataFetcherException() {
                return ConfigFacade.getOptionalValue("mp.graphql.printDataFetcherException", boolean.class)
                                   .orElse(false);
            }

            @Override
            public Optional<List<String>> getHideErrorMessageList() {
                return Optional.ofNullable(ConfigFacade.getOptionalValue(ConfigKey.EXCEPTION_BLACK_LIST, String.class)
                                                       .map(s -> Arrays.asList(s.split(",")))
                                                       .orElse(null));
            }

            @Override
            public Optional<List<String>> getShowErrorMessageList() {
                return Optional.ofNullable(ConfigFacade.getOptionalValue(ConfigKey.EXCEPTION_WHITE_LIST, String.class)
                                                       .map(s -> Arrays.asList(s.split(",")))
                                                       .orElse(null));
            }

            @Override
            public boolean isAllowGet() {
                return ConfigFacade.getOptionalValue("mp.graphql.allowGet", boolean.class)
                                   .orElse(false);
            }

            @Override
            @FFDCIgnore({Throwable.class})
            public boolean isMetricsEnabled() {
                try {
                    return null != Class.forName("org.eclipse.microprofile.metrics.SimpleTimer");
                } catch (Throwable t) {
                    return false;
                }
            }

            @SuppressWarnings("unchecked")
            @Override
            public <T> T getConfigValue(String key, Class<T> type, T defaultValue) {
                if ("smallrye.graphql.metrics.enabled".equals(key)) {
                    return (T) Boolean.TRUE;
                }
                return super.getConfigValue(key, type, defaultValue);
            }

            @Override
            public Optional<List<String>> getUnwrapExceptions() {
                Optional<List<String>> unwrapExceptions = super.getUnwrapExceptions();
                return unwrapExceptions == null ? Optional.empty() : unwrapExceptions;
            }
        };
        diagBag.config = config;
        
        graphQLSchema = Bootstrap.bootstrap(diagBag.modelSchema, config);

        diagBag.graphQLSchema = graphQLSchema;

        if (TraceComponent.isAnyTracingEnabled() && tc.isDebugEnabled()) {
            Tr.debug(tc, "SmallRye GraphQL initialized");
        }

        ctx.setAttribute(SchemaServlet.SCHEMA_PROP, graphQLSchema);

        ExecutionService executionService = new ExecutionService(config, graphQLSchema, null);
        
        String path = "/" + ConfigFacade.getOptionalValue("mp.graphql.contextpath", String.class)
                                        .filter(s -> {return s.replaceAll("/", "").length() > 0;})
                                        .orElse("graphql");
        ExecutionServlet execServlet = new ExecutionServlet(executionService, config);
        ServletRegistration.Dynamic execServletReg = ctx.addServlet(EXECUTION_SERVLET_NAME, execServlet);
        execServletReg.addMapping(path + "/*");
        SchemaPrinter printer = new SchemaPrinter(config);
        diagBag.schemaPrinter = printer;
        ServletRegistration.Dynamic schemaServletReg = ctx.addServlet(SCHEMA_SERVLET_NAME, new SchemaServlet(printer));
        schemaServletReg.addMapping(path + "/schema.graphql");

        if (securityInitializer != null) {
            securityInitializer.onStartup(ctx);
        }

        boolean enableGraphQLUIServlet = ConfigFacade.getOptionalValue("io.openliberty.enableGraphQLUI", boolean.class)
                                                     .orElse(false);
        if (enableGraphQLUIServlet) {
            GraphiQLUIServlet uiServlet = new GraphiQLUIServlet();
            ServletRegistration.Dynamic uiServletReg = ctx.addServlet(UI_SERVLET_NAME, uiServlet);
            uiServletReg.addMapping("/graphql-ui");
        }

        if (TraceComponent.isAnyTracingEnabled() && tc.isDebugEnabled()) {
            Tr.exit(tc, "onStartup");
        }
    }

    // Introspector methods/classes
    @Override
    public String getIntrospectorName() {
        return "MPGraphQLIntrospector";
    }

    @Override
    public String getIntrospectorDescription() {
        return "Diagnostics for GraphQL Applications - schema, model, etc.";
    }

    @Override
    public void introspect(PrintWriter out) throws Exception {

        for (DiagnosticsBag bag : diagnostics.values()) {
            out.println();
            out.println(bag);
            out.println();
        }
    }

    private static class DiagnosticsBag {
        private static String LS = System.lineSeparator();
        String appName;
        URL webinfClassesUrl;
        GraphQLConfig config;
        GraphQLSchema graphQLSchema;
        SchemaPrinter schemaPrinter;
        Schema modelSchema;

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            // app name and location
            sb.append("Diagnostics for ").append(appName != null ? appName : "unknown app").append(" at location: ");
            sb.append(webinfClassesUrl).append(LS);
            
            // config
            sb.append("Config:").append(LS);
            if (config == null) {
                sb.append("  null").append(LS);
            } else {
                sb.append("  ").append("defaultErrorMessage: ").append(config.getDefaultErrorMessage()).append(LS);
                sb.append("  ").append("printDataFetcherException: ").append(config.isPrintDataFetcherException()).append(LS);
                sb.append("  ").append("hideErrorMessageList: ").append(config.getHideErrorMessageList()).append(LS);
                sb.append("  ").append("showErrorMessageList: ").append(config.getShowErrorMessageList()).append(LS);
                sb.append("  ").append("allowGet: ").append(config.isAllowGet()).append(LS);
                sb.append("  ").append("metricsEnabled: ").append(config.isMetricsEnabled()).append(LS);
            }
            
            // schema
            sb.append("Schema:").append(LS);
            if (schemaPrinter == null || graphQLSchema == null) {
                sb.append("  null").append(LS);
            } else {
                sb.append(schemaPrinter.print(graphQLSchema)).append(LS);
            }
            
            
            // model schema - Java class/method/field info:
            sb.append("Model schema:").append(LS);
            if (modelSchema == null) {
                sb.append("  null").append(LS);
            } else {
                // TODO: hoping to add toString() implementations to SmallRye model classes to avoid all this code...
                sb.append("  Queries:").append(LS);
                for (io.smallrye.graphql.schema.model.Operation query : modelSchema.getQueries()) {
                    sb.append("    ").append(toString(query)).append(LS);
                }
                sb.append("  Mutations:").append(LS);
                for (io.smallrye.graphql.schema.model.Operation mutation : modelSchema.getMutations()) {
                    sb.append("    ").append(toString(mutation)).append(LS);
                }
                sb.append("  Types:").append(LS);
                for (String typeName : modelSchema.getTypes().keySet()) {
                    sb.append("    ").append(typeName).append(LS);
                }
                sb.append("  Inputs:").append(LS);
                for (String inputName : modelSchema.getInputs().keySet()) {
                    sb.append("    ").append(inputName).append(LS);
                }
                sb.append("  Interfaces:").append(LS);
                for (String interfaceName : modelSchema.getInterfaces().keySet()) {
                    sb.append("    ").append(interfaceName).append(LS);
                }
                sb.append("  Enums:").append(LS);
                for (String enumName : modelSchema.getEnums().keySet()) {
                    sb.append("    ").append(enumName).append(LS);
                }
            }

            return sb.toString();
        }
        
        static String toString(io.smallrye.graphql.schema.model.Operation o) {
            StringBuilder sb = new StringBuilder();
            sb.append(o.getName()).append(" ").append(o.getClassName()).append(" (prop ")
              .append(o.getPropertyName()).append(") (method ").append(o.getMethodName()).append(") ");
            if (o.isNotNull()) {
                sb.append("NOT_NULL ");
            }
            if (o.hasDefaultValue()) {
                sb.append("DefaultValue = ").append(o.getDefaultValue()).append(" ");
            }
            String description = o.getDescription();
            if (description != null) {
                sb.append("Description: ").append(o.getDescription()); 
            }
            List<io.smallrye.graphql.schema.model.Argument> arguments = o.getArguments();
            if (arguments != null) {
                sb.append("Arguments: ");
                for (io.smallrye.graphql.schema.model.Argument arg : arguments) {
                    if (arg.isSourceArgument()) {
                        sb.append("@Source ");
                    }
                    sb.append(arg.getName()).append(", ");
                }
            }
            return sb.toString();
        }
    }
}
