/**
 * Copyright (c) 2014,2018 by the respective copyright holders.
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.dobiss.internal;

/**
 * The {@link DobissConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Bjorn Aelvoet - Initial contribution
 */
public class DobissConfiguration {

    public String ipAddress;

    public int addressRelay01 = -1;
    public int addressRelay02 = -1;
    public int addressDimmer01 = -1;
    public int addressDimmer02 = -1;

    public int pollingInterval;
}
