/*******************************************************************************
 * Copyright (c) 2013, 2020 IBM Corporation and others.
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
package com.ibm.ws.managedbeans.fat.mb.web;

import javax.annotation.ManagedBean;
import javax.annotation.Resource;
import javax.transaction.TransactionSynchronizationRegistry;

@ManagedBean
public class NonCompManagedBean {
    @Resource(name = "java:global/env/tsr")
    TransactionSynchronizationRegistry javaGlobalTSR;

    @Resource(name = "java:app/env/tsr")
    TransactionSynchronizationRegistry javaAppTSR;

    @Resource(name = "java:module/env/tsr")
    TransactionSynchronizationRegistry javaModuleTSR;
}
