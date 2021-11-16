-include= ~${workspace}/cnf/resources/bnd/feature.props
symbolicName=com.ibm.websphere.appserver.mpGraphQL1.0-appSecurity3.0
visibility=private
IBM-Provision-Capability: \
  osgi.identity; filter:="(&(type=osgi.subsystem.feature)(osgi.identity=io.openliberty.mpGraphQL-2.0))", \
  osgi.identity; filter:="(&(type=osgi.subsystem.feature)(osgi.identity=io.openliberty.appSecurity-4.0))"
-bundles=com.ibm.ws.microprofile.graphql.authorization.jakarta,\
  com.ibm.ws.security.authorization.util.jakarta
IBM-Install-Policy: when-satisfied
kind=beta
edition=core
