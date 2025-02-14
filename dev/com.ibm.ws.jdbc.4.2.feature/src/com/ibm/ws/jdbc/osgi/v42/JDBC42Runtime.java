/*******************************************************************************
 * Copyright (c) 2017,2020 IBM Corporation and others.
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
package com.ibm.ws.jdbc.osgi.v42;

import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.sql.BatchUpdateException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Statement;
import java.util.concurrent.Executor;

import javax.sql.ConnectionPoolDataSource;
import javax.sql.DataSource;
import javax.sql.PooledConnection;
import javax.sql.XAConnection;
import javax.sql.XADataSource;

import org.osgi.framework.Version;
import org.osgi.service.component.annotations.Component;

import com.ibm.websphere.ras.annotation.Trivial;
import com.ibm.ws.jca.adapter.WSConnectionManager;
import com.ibm.ws.jdbc.osgi.JDBCRuntimeVersion;
import com.ibm.ws.rsadapter.impl.StatementCacheKey;
import com.ibm.ws.rsadapter.impl.WSConnectionRequestInfoImpl;
import com.ibm.ws.rsadapter.impl.WSManagedConnectionFactoryImpl;
import com.ibm.ws.rsadapter.impl.WSRdbManagedConnectionImpl;
import com.ibm.ws.rsadapter.jdbc.WSJdbcCallableStatement;
import com.ibm.ws.rsadapter.jdbc.WSJdbcConnection;
import com.ibm.ws.rsadapter.jdbc.WSJdbcDataSource;
import com.ibm.ws.rsadapter.jdbc.WSJdbcDatabaseMetaData;
import com.ibm.ws.rsadapter.jdbc.WSJdbcObject;
import com.ibm.ws.rsadapter.jdbc.WSJdbcPreparedStatement;
import com.ibm.ws.rsadapter.jdbc.WSJdbcResultSet;
import com.ibm.ws.rsadapter.jdbc.WSJdbcStatement;
import com.ibm.ws.rsadapter.jdbc.v41.WSJdbc41Connection;
import com.ibm.ws.rsadapter.jdbc.v42.WSJdbc42CallableStatement;
import com.ibm.ws.rsadapter.jdbc.v42.WSJdbc42DatabaseMetaData;
import com.ibm.ws.rsadapter.jdbc.v42.WSJdbc42PreparedStatement;
import com.ibm.ws.rsadapter.jdbc.v42.WSJdbc42ResultSet;
import com.ibm.ws.rsadapter.jdbc.v42.WSJdbc42Statement;

@Trivial
@Component(property = { "version=4.2", "service.ranking:Integer=42" })
public class JDBC42Runtime implements JDBCRuntimeVersion {
    @Override
    public Version getVersion() {
        return VERSION_4_2;
    }

    @Override
    public WSJdbcDatabaseMetaData newDatabaseMetaData(DatabaseMetaData metaDataImpl,
                                                      WSJdbcConnection connWrapper) throws SQLException {
        return new WSJdbc42DatabaseMetaData(metaDataImpl, connWrapper);
    }

    @Override
    public WSJdbcDataSource newDataSource(WSManagedConnectionFactoryImpl mcf, WSConnectionManager connMgr) {
        return new WSJdbcDataSource(mcf, connMgr);
    }

    @Override
    public WSJdbcStatement newStatement(Statement stmtImplObject, WSJdbcConnection connWrapper, int theHoldability) {
        return new WSJdbc42Statement(stmtImplObject, connWrapper, theHoldability);
    }

    @Override
    public WSJdbcPreparedStatement newPreparedStatement(PreparedStatement pstmtImplObject, WSJdbcConnection connWrapper,
                                                        int theHoldability, String pstmtSQL) throws SQLException {
        return new WSJdbc42PreparedStatement(pstmtImplObject, connWrapper, theHoldability, pstmtSQL);
    }

    @Override
    public WSJdbcPreparedStatement newPreparedStatement(PreparedStatement pstmtImplObject, WSJdbcConnection connWrapper,
                                                        int theHoldability, String pstmtSQL,
                                                        StatementCacheKey pstmtKey) throws SQLException {
        return new WSJdbc42PreparedStatement(pstmtImplObject, connWrapper, theHoldability, pstmtSQL, pstmtKey);
    }

    @Override
    public WSJdbcCallableStatement newCallableStatement(CallableStatement cstmtImplObject, WSJdbcConnection connWrapper,
                                                        int theHoldability, String cstmtSQL) throws SQLException {
        return new WSJdbc42CallableStatement(cstmtImplObject, connWrapper, theHoldability, cstmtSQL);
    }

    @Override
    public WSJdbcCallableStatement newCallableStatement(CallableStatement cstmtImplObject, WSJdbcConnection connWrapper,
                                                        int theHoldability, String cstmtSQL,
                                                        StatementCacheKey cstmtKey) throws SQLException {
        return new WSJdbc42CallableStatement(cstmtImplObject, connWrapper, theHoldability, cstmtSQL, cstmtKey);
    }

    @Override
    public WSJdbcResultSet newResultSet(ResultSet rsImpl, WSJdbcObject parent) {
        return new WSJdbc42ResultSet(rsImpl, parent);
    }

    @Override
    public BatchUpdateException newBatchUpdateException(BatchUpdateException copyFrom, String newMessage) {
        return new BatchUpdateException(newMessage, copyFrom.getSQLState(), copyFrom.getErrorCode(), copyFrom.getLargeUpdateCounts(), null);
    }

    @Override
    public WSJdbcConnection newConnection(WSRdbManagedConnectionImpl mc, Connection conn, Object key, Object currentThreadID) {
        return new WSJdbc41Connection(mc, conn, key, currentThreadID);
    }

    @Override
    public void doSetSchema(Connection sqlConn, String schema) throws SQLException {
        try {
            sqlConn.setSchema(schema);
        } catch (IncompatibleClassChangeError e) { // pre-4.1 driver
            throw new SQLFeatureNotSupportedException(e);
        }
    }

    @Override
    public String doGetSchema(Connection sqlConn) throws SQLException {
        try {
            return sqlConn.getSchema();
        } catch (IncompatibleClassChangeError e) { // pre-4.1 driver
            throw new SQLFeatureNotSupportedException(e);
        }
    }

    @Override
    public void doAbort(final Connection sqlConn, final Executor ex) throws SQLException {
        try {
            AccessController.doPrivileged(new PrivilegedExceptionAction<Void>() {
                @Override
                public Void run() throws SQLException {
                    sqlConn.abort(ex);
                    return null;
                }
            });
        } catch (PrivilegedActionException x) {
            throw (SQLException) x.getCause();
        } catch (IncompatibleClassChangeError e) { // pre-4.1 driver
            throw new SQLFeatureNotSupportedException(e);
        }
    }

    @Override
    public void doSetNetworkTimeout(Connection sqlConn, Executor ex, int millis) throws SQLException {
        try {
            sqlConn.setNetworkTimeout(ex, millis);
        } catch (IncompatibleClassChangeError e) { // pre-4.1 driver
            throw new SQLFeatureNotSupportedException(e);
        }
    }

    @Override
    public int doGetNetworkTimeout(Connection sqlConn) throws SQLException {
        try {
            return sqlConn.getNetworkTimeout();
        } catch (IncompatibleClassChangeError e) { // pre-4.1 driver
            throw new SQLFeatureNotSupportedException(e);
        }
    }

    @Override
    public Connection buildConnection(DataSource ds, String user, String password, WSConnectionRequestInfoImpl cri) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public PooledConnection buildPooledConnection(ConnectionPoolDataSource ds, String user, String password, WSConnectionRequestInfoImpl cri) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public XAConnection buildXAConnection(XADataSource ds, String user, String password, WSConnectionRequestInfoImpl cri) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void doSetShardingKeys(Connection con, Object shardingKey, Object superShardingKey) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public boolean doSetShardingKeysIfValid(Connection con, Object shardingKey, Object superShardingKey, int timeout) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void beginRequest(Connection con) {
    }

    @Override
    public void endRequest(Connection con) {
    }
}
