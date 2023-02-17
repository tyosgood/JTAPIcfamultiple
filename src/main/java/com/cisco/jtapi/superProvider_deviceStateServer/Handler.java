package com.cisco.jtapi.superProvider_deviceStateServer;

// Copyright (c) 2020 Cisco and/or its affiliates.
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
import java.util.*;
import java.util.concurrent.CompletableFuture;

import com.cisco.jtapi.extensions.*;
import com.cisco.cti.util.Condition;

import java.io.IOException;
import java.net.URI;
import java.net.http.*;

public class Handler implements

        ProviderObserver, TerminalObserver {

    public Condition providerInService = new Condition();
    public Condition phoneTerminalInService = new Condition();

    public Map <String, String> devices = new HashMap<String, String>();

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
            //System.out.println("    Received--> Terminal/"+ev);
            //System.out.println("        From Device:"+ ev.getTerminal());
            switch (ev.getID()) {
                case CiscoTermInServiceEv.ID:
                    phoneTerminalInService.set();
                    break;
                case CiscoTermDeviceStateIdleEv.ID:
                    System.out.println("    "+ev.getTerminal()+" STATE--> "+superProvider_deviceStateServer.stateName.get(CiscoTerminal.DEVICESTATE_IDLE) + "--> Light off @ " +superProvider_deviceStateServer.deviceList.get(ev.getTerminal().toString()));
                    lightAction("idle",superProvider_deviceStateServer.deviceList.get(ev.getTerminal().toString()) );
                    break;
                case CiscoTermDeviceStateActiveEv.ID:
                    System.out.println("    "+ev.getTerminal()+" STATE--> "+superProvider_deviceStateServer.stateName.get(CiscoTerminal.DEVICESTATE_ACTIVE)+ "--> Light on @ " +superProvider_deviceStateServer.deviceList.get(ev.getTerminal().toString()));
                    lightAction("active",superProvider_deviceStateServer.deviceList.get(ev.getTerminal().toString()) );
                    break;
                case CiscoTermDeviceStateAlertingEv.ID:
                    System.out.println("    "+ev.getTerminal()+" STATE--> "+superProvider_deviceStateServer.stateName.get(CiscoTerminal.DEVICESTATE_ALERTING)+ "--> Light flashing at .5 sec @ " +superProvider_deviceStateServer.deviceList.get(ev.getTerminal().toString()));
                    lightAction("alerting",superProvider_deviceStateServer.deviceList.get(ev.getTerminal().toString()) );
                    break;
                case CiscoTermDeviceStateHeldEv.ID:
                    System.out.println("    "+ev.getTerminal()+" STATE--> "+superProvider_deviceStateServer.stateName.get(CiscoTerminal.DEVICESTATE_HELD)+ "--> Light flashing at 1.5 sec @ " +superProvider_deviceStateServer.deviceList.get(ev.getTerminal().toString()));
                    lightAction("held",superProvider_deviceStateServer.deviceList.get(ev.getTerminal().toString()) );
                    break;
                case CiscoTermDeviceStateWhisperEv.ID:
                    System.out.println("    "+ev.getTerminal()+" STATE--> "+superProvider_deviceStateServer.stateName.get(CiscoTerminal.DEVICESTATE_WHISPER));
                    break;            }
        }
    }

    public void lightAction(String action, String URL){
        
        HttpClient client = HttpClient.newHttpClient();

        URI myUri = URI.create(URL+action);

        HttpRequest request = HttpRequest.newBuilder(myUri).build();

        //try {
            CompletableFuture response = client.sendAsync(request, HttpResponse.BodyHandlers.ofString());
            //System.out.println(response.body());
        //} catch (IOException e) {
            // TODO Auto-generated catch block
       //     e.printStackTrace();
       // } catch (InterruptedException e) {
            // TODO Auto-generated catch block
      //      e.printStackTrace();
      //  }
    }

}