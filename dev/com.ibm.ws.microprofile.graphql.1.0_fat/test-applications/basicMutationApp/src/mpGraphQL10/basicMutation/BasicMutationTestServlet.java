/*******************************************************************************
 * Copyright (c) 2019 IBM Corporation and others.
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
package mpGraphQL10.basicMutation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.core.Response;

import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.junit.Test;

import componenttest.app.FATServlet;

@SuppressWarnings("serial")
@WebServlet(urlPatterns = "/BasicMutationTestServlet")
public class BasicMutationTestServlet extends FATServlet {
    Logger LOG = Logger.getLogger(BasicMutationTestServlet.class.getName());

    private RestClientBuilder builder;

    private static String getSysProp(String key, String defaultValue) {
        return AccessController.doPrivileged((PrivilegedAction<String>) () -> System.getProperty(key, defaultValue));
    }

    @Override
    public void init() throws ServletException {
        String contextPath = getSysProp("com.ibm.ws.microprofile.graphql.fat.contextpath", "graphql");
        String baseUriStr = "http://localhost:" + getSysProp("bvt.prop.HTTP_default", "8010") + "/basicMutationApp/" + contextPath;
        LOG.info("baseUrl = " + baseUriStr);
        URI baseUri = URI.create(baseUriStr);
        builder = RestClientBuilder.newBuilder()
                        .property("com.ibm.ws.jaxrs.client.receive.timeout", "120000")
                        .property("com.ibm.ws.jaxrs.client.connection.timeout", "120000")
                        .register(LoggingFilter.class)
                        .baseUri(baseUri);
    }

    @Test
    public void testSchemaContainsMutationInfo(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        GraphQLClient client = builder.build(GraphQLClient.class);
        String schema = client.schema();
        System.out.println("Schema: " + System.lineSeparator() + schema);
        assertNotNull(schema);
        assertTrue(schema.contains("Widget"));
        assertTrue(schema.contains("createWidget"));
        assertTrue(schema.contains("Create a new widget for sale."));
        assertTrue(schema.contains("WidgetInput"));
        assertTrue(schema.contains("A for-sale item."));
        assertTrue(schema.contains("quantityOnAllWidgets(newQuantity: Int!): [Widget]"));
        assertFalse(schema.contains("setQuantityOnAllWidgets"));
    }

    @Test
    public void testMutation(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        GraphQLClient client = builder.build(GraphQLClient.class);
        GraphQLOperation graphQLOperation = new GraphQLOperation();
        graphQLOperation.setOperationName("createWidget");
        graphQLOperation.setQuery("mutation createWidget ($widget: WidgetInput) {" + System.lineSeparator() +
                                  "  createWidget(widget: $widget) {" + System.lineSeparator() +
                                  "    name," + System.lineSeparator() +
                                  "    weight," + System.lineSeparator() +
                                  "  }" + System.lineSeparator() +
                                  "}");
        graphQLOperation.setVariables(Variables.newVars("Earbuds", 20, 1.2));
        WidgetQueryResponse response = client.allWidgets(graphQLOperation);
        System.out.println("Mutation Response: " + response);
        Widget widget = response.getData().getCreateWidget();
        assertNotNull(widget);
        assertEquals("Earbuds", widget.getName());
        assertEquals(-1, widget.getQuantity()); // Quantity was not specified in mutation request
        assertEquals(1.2, widget.getWeight(), 0.1);
        
        
        graphQLOperation = new GraphQLOperation();
        graphQLOperation.setOperationName("allWidgets");
        graphQLOperation.setQuery("query allWidgets {" + System.lineSeparator() +
                       "  allWidgets {" + System.lineSeparator() +
                       "    name," + System.lineSeparator() +
                       "    quantity" + System.lineSeparator() +
                       "  }" + System.lineSeparator() +
                       "}");

        response = client.allWidgets(graphQLOperation);
        System.out.println("Query Response: " + response);
        List<Widget> widgets = response.getData().getAllWidgets();
        assertEquals(1, widgets.size());
        widget = widgets.get(0);
        assertNotNull(widget);
        assertEquals("Earbuds", widget.getName());
        assertEquals(20, widget.getQuantity());
        assertEquals(-1.0, widget.getWeight(), 0.1); // weight wasn't specified in the query
    }
}
