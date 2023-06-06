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
    //Create Array list to hold the address objects
    public static List<AddressImpl> addressList = new ArrayList<AddressImpl>();

    public static String targetDN = "2111";
    

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

        

        for (String extension : extensions){
            log("Creating address for extension: " + extension);
            addressList.add((AddressImpl) provider.getAddress(extension)); 
        }       

        /* // Open the ALICE_DN Address and wait for it to go in service
        log("Opening fromAddress DN: " + dotenv.get("ALICE_DN"));
        CiscoAddress fromAddress = (CiscoAddress) provider.getAddress(dotenv.get("ALICE_DN"));
        log("Awaiting CiscoAddrInServiceEv for: " + fromAddress.getName() + "...");
        fromAddress.addObserver(handler);
        handler.fromAddressInService.waitTrue();
        // Add a call observer to receive call events
        fromAddress.addCallObserver(handler);
        // Get/open the first Terminal for the Address.  Could be multiple
        //   if it's a shared line*/
        CiscoTerminal fromTerminal = (CiscoTerminal) provider.createTerminal("SEP28DFEBB58EE8");
        log("Awaiting CiscoTermInServiceEv for: " + fromTerminal.getName() + "...");
        fromTerminal.addObserver(handler);
        handler.fromTerminalInService.waitTrue(); 
        
        CiscoTermEvFilter termFilter = fromTerminal.getFilter();

        termFilter.setButtonPressedEnabled(true);

        termFilter.setDeviceStateIdleEvFilter(true);
        termFilter.setDeviceStateActiveEvFilter(true);
        termFilter.setDeviceStateAlertingEvFilter(true);
        termFilter.setDeviceStateHeldEvFilter(true);
        termFilter.setDeviceStateWhisperEvFilter(false); 

        fromTerminal.setFilter(termFilter);

        //log terminal montior
        log("Monitoring state changes for: "+fromTerminal.getName()+"...");

       // AddressImpl fwdAddress = null;

        //Set up forwarding destination
    //CallControlForwarding[] cforwardIs = new CallControlForwarding[1];
    //cforwardIs[0] = new CallControlForwarding(targetDN);

        /* for (String extension : extensions){
            log(extension);
        fwdAddress = null;

         //Try to set a forwarder
        fwdAddress  =  (AddressImpl) provider.getAddress(extension);
        fwdAddress.addObserver(handler);
        handler.fromAddressInService.waitTrue();

         if (fwdAddress.getForwarding() != null) {
            log ("forwading for "+extension+ " set to "+ fwdAddress.getForwarding()[0].getDestinationAddress());
            log ("Canceling existing forward");
            fwdAddress.cancelForwarding();
        }
        
      
      
      fwdAddress.setForwarding(cforwardIs);
      log("Setting cfwdAll for "+ extension +" to 2111");
      
    } */
    for (int i = 0; i < addressList.size(); i++){
        //add observer for each terminal
       addressList.get(i).addObserver(handler);
       addressList.get(i).addCallObserver(handler);

     
    }

    
          //log ("forwading for "+dotenv.get("ALICE_DN")+ " is now set to "+ fwdAddress.getForwarding()[0].getDestinationAddress());
          //System.exit(0);
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

