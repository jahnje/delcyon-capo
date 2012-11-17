Runtime Requirements: Java 6 or better.

IDE Requirements: Eclipse

Build Requirements: ant

License: GPLv3, parts (eu.medsea) are under the Apache license. 

Documentation: http://capo.delcyon.com

Source Code: http://github.com/jahnje/delcyon-capo

Mailing List: capo@delcyon.com

Mailing List Archive: http://groups.delcyon.com/capo

build: ant [clean|dist|patch|build|minor|major] this will result in a distribution directory.
	The dist option just builds a distribution w/o updating any build numbers. 

install: java -jar client-x.x.x.x-dist.jar or server-x.x.x.x-dist.jar
	The Capo server install includes the client. Once the server is up and running you may download the client from the server using a command like:  'wget 10.10.4.242:2442/client-dist.jar'. And install as above. 

run: capo/[server|client]/bin/capo-[server|client].sh 
	Or in /etc/rc.d/init.d/capo-server or capo-client

Status: alpha 

You can download a pre-built distribution from here https://github.com/downloads/jahnje/delcyon-capo/capo-server-0.1.3.0-dist.jar