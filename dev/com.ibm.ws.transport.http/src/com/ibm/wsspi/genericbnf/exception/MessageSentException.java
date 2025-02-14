/*******************************************************************************
 * Copyright (c) 2004, 2009 IBM Corporation and others.
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
package com.ibm.wsspi.genericbnf.exception;

/**
 * Thrown when attempting to send a message that has already been sent.
 * 
 * @ibm-private-in-use
 */
public class MessageSentException extends Exception {

    /** Serialization ID value */
    static final private long serialVersionUID = 7623775801310848408L;

    /**
     * Constructor for the method sent exception
     * 
     * @param message
     */
    public MessageSentException(String message) {
        super(message);
    }
}
