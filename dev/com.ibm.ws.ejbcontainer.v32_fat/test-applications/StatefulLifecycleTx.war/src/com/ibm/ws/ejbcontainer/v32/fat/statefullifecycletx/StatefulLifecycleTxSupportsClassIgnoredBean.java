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
import javax.annotation.PreDestroy;
import javax.ejb.PostActivate;
import javax.ejb.PrePassivate;
import javax.ejb.Stateful;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

@Stateful
@TransactionAttribute(TransactionAttributeType.SUPPORTS)
@SuppressWarnings("serial")
public class StatefulLifecycleTxSupportsClassIgnoredBean extends AbstractStatefulLifecycleTxBean {
    private static final Logger logger = Logger.getLogger(StatefulLifecycleTxSupportsClassIgnoredBean.class.getName());

    @PostConstruct
    private void postConstruct() {
        logger.info("postConstruct");
        checkLocalUOW("PostConstruct");
    }

    @PrePassivate
    private void prePassivate() {
        logger.info("prePassivate");
        checkLocalUOW("PrePassivate");
    }

    @PostActivate
    private void postActivate() {
        logger.info("postActivate");
        checkLocalUOW("PostActivate");
    }

    @PreDestroy
    private void preDestroy() {
        logger.info("preDestroy");
        checkLocalUOW("PreDestroy");
    }
}
