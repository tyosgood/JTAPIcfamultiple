package com.cisco.jtapi.cfamultiple;

import java.time.format.DateTimeFormatter;  
import java.time.LocalDateTime;
import java.util.*;
import java.io.*;  

import javax.telephony.*;
import javax.telephony.callcontrol.*;
import com.cisco.jtapi.*;
import com.cisco.jtapi.extensions.*;


import io.github.cdimascio.dotenv.Dotenv;

public class callForward {
    private static DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss.SS"); 

    private static void log(String msg) {
        System.out.println(dtf.format(LocalDateTime.now()) + " " + msg);
    }

    public static List<String> extensions = readExtensionFile();

    public static void main(String[] args) throws

    JtapiPeerUnavailableException, ResourceUnavailableException, MethodNotSupportedException, InvalidArgumentException,
            PrivilegeViolationException, InvalidPartyException, InvalidStateException, InterruptedException {

        // Retrieve environment variables from .env, if present
        Dotenv dotenv=Dotenv.load();

        // The Handler class provides observers for provider/address/terminal/call events
        Handler handler = new Handler();

        // Create the JtapiPeer object, representing the JTAPI library
        log("Initializing Jtapi");
        CiscoJtapiPeer peer = (CiscoJtapiPeer) JtapiPeerFactory.getJtapiPeer(null);

        // Create and open the Provider, representing a JTAPI connection to CUCM CTI
        // Manager
        String providerString = String.format(
            "%s;login=%s;passwd=%s",
            dotenv.get("CUCM_ADDRESS"),
            dotenv.get("JTAPI_USERNAME"),
            dotenv.get("JTAPI_PASSWORD"));
        log("Connecting Provider: " + providerString);
        CiscoProvider provider=(CiscoProvider) peer.getProvider(providerString);
        log("Awaiting ProvInServiceEv...");
        provider.addObserver(handler);
        handler.providerInService.waitTrue();

        /* // Open the ALICE_DN Address and wait for it to go in service
        log("Opening fromAddress DN: " + dotenv.get("ALICE_DN"));
        CiscoAddress fromAddress = (CiscoAddress) provider.getAddress(dotenv.get("ALICE_DN"));
        log("Awaiting CiscoAddrInServiceEv for: " + fromAddress.getName() + "...");
        fromAddress.addObserver(handler);
        handler.fromAddressInService.waitTrue();
        // Add a call observer to receive call events
        fromAddress.addCallObserver(handler);
        // Get/open the first Terminal for the Address.  Could be multiple
        //   if it's a shared line
        CiscoTerminal fromTerminal = (CiscoTerminal) fromAddress.getTerminals()[0];
        log("Awaiting CiscoTermInServiceEv for: " + fromTerminal.getName() + "...");
        fromTerminal.addObserver(handler);
        handler.fromTerminalInService.waitTrue(); */

        for (String extension : extensions){
            log(extension);
        }

        //Try to set a forwarder
        AddressImpl fwdAddress =  (AddressImpl) provider.getAddress(dotenv.get("ALICE_DN"));
        fwdAddress.addObserver(handler);
        handler.fromAddressInService.waitTrue();

        if (fwdAddress.getForwarding() != null) {
            log ("forwading for "+dotenv.get("ALICE_DN")+ " set to "+ fwdAddress.getForwarding()[0].getDestinationAddress());
            log ("Canceling existing forward");
            fwdAddress.cancelForwarding();
        }
        
      CallControlForwarding[] cforwardIs = new CallControlForwarding[1];
      cforwardIs[0] = new CallControlForwarding("2111");
      fwdAddress.setForwarding(cforwardIs);
      //log ("forwading for "+dotenv.get("ALICE_DN")+ " is now set to "+ fwdAddress.getForwarding()[0].getDestinationAddress());
    }
    
    public static List<String> readExtensionFile() {
       
          BufferedReader br = null;
          List<String> extensions = new ArrayList<String>();
  
          try {
  
              // create file object
              File file = new File("src/main/java/com/cisco/jtapi/cfamultiple/extensionList.csv");
  
              // create BufferedReader object from the File
              br = new BufferedReader(new FileReader(file));
  
              String line = null;
              
  
              // read file line by line
              while ((line = br.readLine()) != null) {
  
                  // split the line by :
                extensions.add(line);
                
              }
          }
          catch (Exception e) {
              e.printStackTrace();
          }
          finally {
  
              // Always close the BufferedReader
              if (br != null) {
                  try {
                      br.close();
                  }
                  catch (Exception e) {
                  };
              }
          }
  
          return extensions;
    }
        
  
   
}

