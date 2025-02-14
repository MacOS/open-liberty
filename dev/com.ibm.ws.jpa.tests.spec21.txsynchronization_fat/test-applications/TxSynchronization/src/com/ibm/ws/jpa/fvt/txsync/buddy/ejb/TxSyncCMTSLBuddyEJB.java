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

package com.ibm.ws.jpa.fvt.txsync.buddy.ejb;

import java.io.Serializable;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.ejb.EJBContext;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagement;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;
import javax.persistence.SynchronizationType;

import com.ibm.ws.jpa.fvt.txsync.buddy.ejblocal.TxSyncCMTSLBuddyLocal;
import com.ibm.ws.jpa.fvt.txsync.testlogic.TargetEntityManager;
import com.ibm.ws.jpa.fvt.txsync.testlogic.TestWorkRequest;

@Stateless(name = "TxSyncCMTSLBuddyEJB")
@Local(TxSyncCMTSLBuddyLocal.class)
@TransactionManagement(javax.ejb.TransactionManagementType.CONTAINER)
public class TxSyncCMTSLBuddyEJB {
    // Container Managed Transaction Scope
    @PersistenceContext(unitName = "TxSync",
                        type = PersistenceContextType.TRANSACTION,
                        synchronization = SynchronizationType.SYNCHRONIZED)
    private EntityManager emCMTSTxSync;

    @PersistenceContext(unitName = "TxSync",
                        type = PersistenceContextType.TRANSACTION,
                        synchronization = SynchronizationType.UNSYNCHRONIZED)
    private EntityManager emCMTSTxUnsync;

    // Container Managed Transaction Scope #2
    @PersistenceContext(unitName = "TxSync2",
                        type = PersistenceContextType.TRANSACTION,
                        synchronization = SynchronizationType.SYNCHRONIZED)
    private EntityManager emCMTSTxSync2;

    @PersistenceContext(unitName = "TxSync2",
                        type = PersistenceContextType.TRANSACTION,
                        synchronization = SynchronizationType.UNSYNCHRONIZED)
    private EntityManager emCMTSTxUnsync2;

    @Resource
    protected EJBContext ejbCtx;

    @PostConstruct
    protected void postConstruct() {

    }

    @PreDestroy
    protected void preDestroy() {

    }

    /*
     * Local Interface Methods
     */
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public Serializable doWorkRequestWithTxMandatory(TestWorkRequest work, TargetEntityManager targetEm) {
        return work.doTestWork(getEntityManager(targetEm), null, this);
    }

    @TransactionAttribute(TransactionAttributeType.NEVER)
    public Serializable doWorkRequestWithTxNever(TestWorkRequest work, TargetEntityManager targetEm) {
        return work.doTestWork(getEntityManager(targetEm), null, this);
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public Serializable doWorkRequestWithTxNotSupported(TestWorkRequest work, TargetEntityManager targetEm) {
        return work.doTestWork(getEntityManager(targetEm), null, this);
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public Serializable doWorkRequestWithTxRequired(TestWorkRequest work, TargetEntityManager targetEm) {
        return work.doTestWork(getEntityManager(targetEm), null, this);
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public Serializable doWorkRequestWithTxRequiresNew(TestWorkRequest work, TargetEntityManager targetEm) {
        return work.doTestWork(getEntityManager(targetEm), null, this);
    }

    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public Serializable doWorkRequestWithTxSupports(TestWorkRequest work, TargetEntityManager targetEm) {
        return work.doTestWork(getEntityManager(targetEm), null, this);
    }

    private EntityManager getEntityManager(TargetEntityManager targetEm) {
        EntityManager workEntityManager = null;

        switch (targetEm) {
            case TXSYNC1_SYNCHRONIZED:
                workEntityManager = emCMTSTxSync;
                break;
            case TXSYNC1_UNSYNCHRONIZED:
                workEntityManager = emCMTSTxUnsync;
                break;
            case TXSYNC2_SYNCHRONIZED:
                workEntityManager = emCMTSTxSync2;
                break;
            case TXSYNC2_UNSYNCHRONIZED:
                workEntityManager = emCMTSTxUnsync2;
                break;
            default:
                throw new RuntimeException("Unknown TargetEntityManager type: " + targetEm);
        }

        return workEntityManager;
    }
}
