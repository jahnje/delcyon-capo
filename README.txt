Capo System Manager Overview
What does Capo System Manager do? 
  Capo System Manager software provides reusable system, network, service and command line templates to assist system administrators in the set up, maintenance and documentation of their computer networks. 

What is Capo System Manager?   
    Capo is a Java and XML based, template driven, extensible, system configuration program. Its purpose is to configure any UNIX, Windows or Macintosh system based on a collection of system, network and service templates located on a central server.  It consists of a Java based server component, and one Java based client component per controlled system. The Capo server uses an XML based language to issue commands and copy files to and from remotely controlled systems, as well as perform other tasks and gather remote information.

Why templates? 
    Capo's use of templates allows for standardization of configurations across a large collection of systems. Capo System Manager is designed on the principle that most systems are configured in the same basic fashion and are then differentiated from each other to provide a set of specific services. So most systems can be viewed as adhering to a basic system template and then having specific network and service templates layered on top that. When there are just a few systems an administrator can keep track of most of the information need to configure and maintain them, but as the number of services grows an administrator's ability to keep track of that information will decrease, even with excellent documentation. 

How does Capo System Manager make changes to my network easier? 
   Using Capo System Manager, changes to a system, network, service or command line template on the server will  automatically propagate that change to each client on the network using that template. The client can be configured to check in with the server for changes to templates at the desired interval.

Capo System Manager makes it easier to apply updates: 
Maintaining secure systems often requires that the same patch be installed on each server that needs the patch on the network. Using Capo System Manager, the client will check in with the server, find the patch it needs, install it and then report its success or failure. 

Capo System Manager makes it easier to replace existing systems: 
The reality of system administration is that as systems age, new systems will be brought online to replace them, but will need the same services installed and configured as its predecessor. Capo allows the new system to copy and use the service templates the existing service is running. 

Capo System Manager makes it easier to maintain systems on different networks: 
Sometimes system administrators need to maintain similar systems on different networks. Capo System Manager provides template variables which allow systems to request the proper network information and still use the same system, network and service templates. 

 How does Capo System Manager help with documentation of the network? 
     As the requirements of auditing and standardization grow, so do the number of systems, and the difficulty in maintaining and documenting them. With PCI-DSS, the cloud, and virtualization, documentation of systems and networks becomes even more important. 
   Capo System Manager's use of templates allows for centralized self documenting of systems and services. Capo System Manager can run a command, parse the output of a command, append the parsed output to the XML file and then store that XML file in a centralized location. It is easy to document what commands were run to what effect. 

Is Capo System Manager Extensible? 
    Capo's extensibility exists on three levels. The most simple of these is its ability to allow an administrator to create a library of custom command line output parsers and templates using XSLT, and regular expressions. Almost any output or file can be parsed so that automated decisions can be made regardless of whether or not  the base Capo software is familiar with it. Since Capo runs commands from the system shell, any scripts already in use can be utilized  and stored in centralized locations of the administrators choosing. Any of Capo's elements or it's content can be stored in separate files and included at any point to allow an administrator make as modular a system as they like. 
   The second kind of extensibility provided by Capo is that it is plugin driven. Custom Java based control elements can be written to add additional functionality on both the server and the client sides. Capo exposes its storage, transport, data model, XML parsing, and control APIs to every plugin. Every part of Capo has been designed to allow additional functionality to be injected. 
   The third and final piece of extensibility is that Capo is open sourced under the LGPL license. A Java programmer has full access to any and all of the source code, and can rewrite or modify the code in anyway they see fit. There is no vendor lock-in nor will the software ever be unmaintained. 

Other Concepts and Features:
Other Data Sources:
Capo can use any resource on a filesystem or the web with knowledge of which client is asking for it to provide client specific configuration and information. An example of this would be a VPN server with unique keys named for each client, that must be located and then installed on each client. A Capo configuration requires one line to locate and copy the appropriate key to the client and one more line to restart the VPN connection if needed. 
Data Aggregation:
Capo can use the information provided by clients to create dynamic configuration files for other clients. DNS entries,  routing tables, etc. can be created on the fly for one client based on information gathered about the the other clients.
Delegation:
Capo can function as both a client and a server, so one Capo server can modify another, spreading the load across a number of servers. This can also be used for redundancy and failover.
Failover:
Capo clients can use a list of servers and will choose the first available one to check in with. These servers do not have to have the same information, and can therefor be used to put the client into a degraded state until it's primary server comes back online.