/*******************************************************************************
 * Copyright (c) 2014, 2024 IBM Corporation and others.
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
package com.ibm.ws.ejbcontainer.v32.fat.statefullifecycletx;

import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.LocalHome;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.ejb.Stateful;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

@Stateful
@LocalHome(StatefulLifecycleTx2xHome.class)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@SuppressWarnings("serial")
public class StatefulLifecycleTxRequiredClass2xIgnoredBean extends AbstractStatefulLifecycleTxBean implements SessionBean {
    private static final Logger logger = Logger.getLogger(StatefulLifecycleTxRequiredClass2xIgnoredBean.class.getName());

    @Override
    public void setSessionContext(SessionContext context) {
    }

    @PostConstruct
    public void postConstruct() {
        logger.info("postConstruct");
        checkLocalUOW("PostConstruct");
    }

    // @Init - implicit
    public void ejbCreate() {
        logger.info("ejbCreate");
        checkLocalUOW("Init");
    }

    @Override
    public void ejbPassivate() {
        logger.info("ejbPassivate");
        checkLocalUOW("PrePassivate");
    }

    @Override
    public void ejbActivate() {
        logger.info("ejbActivate");
        checkLocalUOW("PostActivate");
    }

    @Override
    public void ejbRemove() {
        logger.info("ejbRemove");
        checkLocalUOW("PreDestroy");
    }
}
