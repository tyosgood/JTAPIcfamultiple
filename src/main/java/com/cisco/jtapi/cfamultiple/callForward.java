package com.cisco.jtapi.cfamultiple;


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

import java.time.format.DateTimeFormatter;  
import java.time.LocalDateTime;
import java.util.*;
import java.io.*;  

import javax.telephony.*;
import com.cisco.jtapi.*;
import com.cisco.jtapi.extensions.*;

import static spark.Spark.*;


import io.github.cdimascio.dotenv.Dotenv;


public class callForward {
    private static DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss.SS"); 

    private static void log(String msg) {
        System.out.println(dtf.format(LocalDateTime.now()) + " " + msg);
    }

    public static List<String> extensions = readExtensionFile();
    //Create Array list to hold the address objects
    public static List<AddressImpl> addressList = new ArrayList<AddressImpl>();

    //need to decalre so we can display
    public static String targetDN = "";
    
    
    

    public static void main(String[] args) throws

    JtapiPeerUnavailableException, ResourceUnavailableException, MethodNotSupportedException, InvalidArgumentException,
            PrivilegeViolationException, InvalidPartyException, InvalidStateException, InterruptedException {

        // Retrieve environment variables from .env, if present
        Dotenv dotenv=Dotenv.load();

        //change this to dynamically grab the IP of the host container
        final String serverIP = "localhost";

        //4567 is default for the SparkJava server, change here to use different;
        final String serverPort = "4567";
        port(Integer.parseInt(serverPort));

        // The Handler class provides observers for provider/address/terminal/call events
        Handler handler = new Handler();

        
        //Web paths for the IP phone service
        get("/setCFwdALL", (req, res) -> {
            log("User entered "+ req.queryParams("target"));
            targetDN = req.queryParams("target");
            
            return handler.setForwards();
        });
            

        get("/unsetCFwdALL", (req, res) -> {
            handler.clearForwards();
            return ("Unsetting Call Forwards");
                });
        
         get("/cfaMultiple", (req, res) -> {
            res.type("text/xml; charset=ISO-8859-1");
            log(req.ip());
            log(req.userAgent());

            return("<CiscoIPPhoneInput>"
                        +"<Title>Set Call Forwards</Title>"
                            +"<Prompt>Enter number</Prompt>"
                                +"<URL>http://"+serverIP+":"+serverPort+"/setCFwdALL</URL>"
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
                                    +"<URL>http://"+serverIP+":"+serverPort+"/unsetCFwdALL</URL>"
                                    +"<Position>2</Position>"
                                +"</SoftKeyItem>"
                                +"<SoftKeyItem>"
                                    +"<Name>Bksp</Name>"
                                    +"<URL>SoftKey:&lt;&lt;</URL>"
                                    +"<Position>3</Position>"
                                +"</SoftKeyItem>"
                                +"<SoftKeyItem>"
                                    +"<Name>Exit</Name>"
                                    +"<URL>SoftKey:Exit</URL>"
                                    +"<Position>4</Position>"
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

         

        for (int i = 0; i < addressList.size(); i++){
            //add observer for each DN in list
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

