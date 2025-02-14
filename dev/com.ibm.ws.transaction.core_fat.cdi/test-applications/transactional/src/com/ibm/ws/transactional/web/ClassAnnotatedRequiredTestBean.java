/*******************************************************************************
 * Copyright (c) 2017, 2020 IBM Corporation and others.
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
package com.ibm.ws.transactional.web;

import java.io.Serializable;

import javax.enterprise.context.SessionScoped;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

@Transactional(value = TxType.REQUIRED,
               rollbackOn = { IllegalAccessException.class, InterruptedException.class },
               dontRollbackOn = { ArithmeticException.class, NullPointerException.class })
@SessionScoped
public class ClassAnnotatedRequiredTestBean extends POJO implements Serializable {

    /**  */
    private static final long serialVersionUID = 1195989801593160231L;
}