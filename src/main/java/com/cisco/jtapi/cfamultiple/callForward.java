package com.cisco.jtapi.cfamultiple;

import java.time.format.DateTimeFormatter;  
import java.time.LocalDateTime;
import java.util.*;
import java.io.*;  

import javax.telephony.*;
import javax.telephony.callcontrol.*;
import com.cisco.jtapi.*;
import com.cisco.jtapi.extensions.*;

import static spark.Spark.*;


import io.github.cdimascio.dotenv.Dotenv;
import spark.Response;

public class callForward {
    private static DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss.SS"); 

    private static void log(String msg) {
        System.out.println(dtf.format(LocalDateTime.now()) + " " + msg);
    }

    public static List<String> extensions = readExtensionFile();
    //Create Array list to hold the address objects
    public static List<AddressImpl> addressList = new ArrayList<AddressImpl>();

    public static String targetDN = "";
    

    public static void main(String[] args) throws

    JtapiPeerUnavailableException, ResourceUnavailableException, MethodNotSupportedException, InvalidArgumentException,
            PrivilegeViolationException, InvalidPartyException, InvalidStateException, InterruptedException {

        // Retrieve environment variables from .env, if present
        Dotenv dotenv=Dotenv.load();

        // The Handler class provides observers for provider/address/terminal/call events
        Handler handler = new Handler();

        get("/setCFwdALL", (req, res) -> {
            log("User entered "+ req.queryParams("target"));
            targetDN = req.queryParams("target");
            
            return handler.setForwards();
        });
            //return ("Setting Call Forwards to "+targetDN); });

        get("/unsetCFwdALL", (req, res) -> {
            handler.clearForwards();
            return ("Unsetting Call Forwards");
                });
        
         get("/test", (req, res) -> {
                    res.type("text/xml; charset=ISO-8859-1");
                    log(req.ip());
                    log(req.userAgent());

                    //return ("<CiscoIPPhoneMenu><Title>Call Forward Multiple</Title><Prompt></Prompt><MenuItem><Name>Set Call Forwards</Name><URL>http://localhost:4567/setCFwdALL</URL></MenuItem><MenuItem><Name>UnSet Call Forwards</Name><URL>http://localhost:4567/unsetCFwdALL</URL></MenuItem></CiscoIPPhoneMenu>"); });
                    return("<CiscoIPPhoneInput>"
                              +"<Title>Set Call Forwards</Title>"
                                    +"<Prompt>Enter number</Prompt>"
                                        +"<URL>http://localhost:4567/setCFwdALL</URL>"
                                        +"<InputItem>"
                                            +"<DisplayName>Number</DisplayName>"
                                            +"<QueryStringParam>target</QueryStringParam>"
                                            +"<DefaultValue>"+targetDN+"</DefaultValue>"
                                            +"<InputFlags>T</InputFlags>"
                                        +"</InputItem>"
                                        +"<SoftKeyItem>"
                                            +"<Name>Fwd</Name>"
                                            +"<URL>SoftKey:Submit</URL>"
                                            +"<Position>1</Position>"
                                        +"</SoftKeyItem>"
                                        +"<SoftKeyItem>"
                                            +"<Name>Un-Fwd</Name>"
                                            +"<URL>http://localhost:4567/unsetCFwdALL</URL>"
                                            +"<Position>2</Position>"
                                        +"</SoftKeyItem>"
                                        +"<SoftKeyItem>"
                                            +"<Name>Bksp</Name>"
                                            +"<URL>SoftKey:&lt;&lt;</URL>"
                                            +"<Position>3</Position>"
                                        +"</SoftKeyItem>"
                            +"</CiscoIPPhoneInput>");
                        });
                    
                  
        

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


    for (int i = 0; i < addressList.size(); i++){
        //add observer for each terminal
       addressList.get(i).addObserver(handler);
       addressList.get(i).addCallObserver(handler);

     
    }
        
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

