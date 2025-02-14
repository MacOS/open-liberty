/*******************************************************************************
 * Copyright (c) 2002, 2021 IBM Corporation and others.
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
package com.ibm.ejb2x.base.cache.ejb;

import javax.ejb.CreateException;
import javax.ejb.EJBLocalHome;

/**
 * Local Home interface for a basic Stateful Session bean that may be configured
 * with different Activation policies (ONCE, TRANSACTION).
 **/
public interface InitRecoveryLogHome extends EJBLocalHome {
    public InitRecoveryLogObj create() throws CreateException;
}