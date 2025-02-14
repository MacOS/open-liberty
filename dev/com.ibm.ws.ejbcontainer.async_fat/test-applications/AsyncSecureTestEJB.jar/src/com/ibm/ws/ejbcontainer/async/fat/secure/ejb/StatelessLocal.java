/*******************************************************************************
 * Copyright (c) 2009, 2019 IBM Corporation and others.
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
package com.ibm.ws.ejbcontainer.async.fat.secure.ejb;

import java.util.concurrent.Future;

/**
 * Local interface for Basic Container Managed Transaction Stateless
 * Session bean.
 **/
public interface StatelessLocal {

    public Future<String> role1Only();

    public Future<String> role2Only();

    public String role3Only();

}
