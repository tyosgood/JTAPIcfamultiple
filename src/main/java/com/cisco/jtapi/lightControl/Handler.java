package com.cisco.jtapi.lightControl;

// Copyright (c) 2023 Cisco and/or its affiliates.
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
// The above copyright notice and this permission notice shall be included in all
// copies or substantial portions of the Software.
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// SOFTWARE.

import javax.telephony.*;
import javax.telephony.events.*;
import com.cisco.jtapi.extensions.*;
import com.cisco.cti.util.Condition;




public class Handler implements ProviderObserver, TerminalObserver {

    public Condition providerInService = new Condition();
    public Condition phoneTerminalInService = new Condition();

     public void providerChangedEvent(ProvEv[] events) {
        for (ProvEv ev : events) {
            System.out.println("    Received--> Provider/" + ev);
            switch (ev.getID()) {
                case ProvInServiceEv.ID:
                    providerInService.set();
                    break;
            }
        }
    }

    public void terminalChangedEvent(TermEv[] events) {
        for (TermEv ev : events) {
           //in the following cases change out "cyberData" to "shelly" to utilize the Shelly smart relays and vice versa
            switch (ev.getID()) {
                case CiscoTermInServiceEv.ID:
                    phoneTerminalInService.set();
                    break;
                case CiscoTermDeviceStateIdleEv.ID:
                    System.out.println("    "+ev.getTerminal()+" STATE--> "+superProvider_deviceStateServer.stateName.get(CiscoTerminal.DEVICESTATE_IDLE) + "--> Light off @ " +superProvider_deviceStateServer.deviceList.get(ev.getTerminal().toString()));
                    cyberData.lightAction("idle",superProvider_deviceStateServer.deviceList.get(ev.getTerminal().toString()) );
                    break;
                case CiscoTermDeviceStateActiveEv.ID:
                    System.out.println("    "+ev.getTerminal()+" STATE--> "+superProvider_deviceStateServer.stateName.get(CiscoTerminal.DEVICESTATE_ACTIVE)+ "--> Light on @ " +superProvider_deviceStateServer.deviceList.get(ev.getTerminal().toString()));
                    cyberData.lightAction("active",superProvider_deviceStateServer.deviceList.get(ev.getTerminal().toString()) );
                    break;
                case CiscoTermDeviceStateAlertingEv.ID:
                    System.out.println("    "+ev.getTerminal()+" STATE--> "+superProvider_deviceStateServer.stateName.get(CiscoTerminal.DEVICESTATE_ALERTING)+ "--> Light flashing at .5 sec @ " +superProvider_deviceStateServer.deviceList.get(ev.getTerminal().toString()));
                    cyberData.lightAction("alerting",superProvider_deviceStateServer.deviceList.get(ev.getTerminal().toString()) );
                    break;
                case CiscoTermDeviceStateHeldEv.ID:
                    System.out.println("    "+ev.getTerminal()+" STATE--> "+superProvider_deviceStateServer.stateName.get(CiscoTerminal.DEVICESTATE_HELD)+ "--> Light flashing at 1.5 sec @ " +superProvider_deviceStateServer.deviceList.get(ev.getTerminal().toString()));
                    cyberData.lightAction("held",superProvider_deviceStateServer.deviceList.get(ev.getTerminal().toString()) );
                    break;
                case CiscoTermDeviceStateWhisperEv.ID:
                    System.out.println("    "+ev.getTerminal()+" STATE--> "+superProvider_deviceStateServer.stateName.get(CiscoTerminal.DEVICESTATE_WHISPER));
                    break;         
                }
        }
    }
}