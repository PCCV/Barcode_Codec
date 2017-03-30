# Barcode_Codec - plugin for ImageJ to Encode/Decode 1D/2D Barcode
===============================
Côme PASQUALIN - François GANNIER (gannier at univ-tours dot fr) 
University of Tours (France)

Date : 
2016/02/01: 0.1 first version
2017/02/03: 0.2 - Show detected barcode format
2017/03/30: 0.3 - add decoding on selection

Please visit http://pccv.univ-tours.fr/ImageJ/Barcode_Codec/ to download and for more information

Tested on ImageJ 1.51g (linux, Mac OS and Windows)

Description
------------
This work is based on the ZXing library.

Installation
------------
For Java 1.6 : download version 2.3 of ZXing core and version 2.2 of ZXing Java SE extensions and copy them in the jars folder. (for Java 1.8 use the latest version)
Download and copy Barcode_Codec.jar in the plugins folder, then restart ImageJ.

User guide
----------
Encoding : Open a new Text window, write or paste your text then simply use the Barcode_Codec plugin to encode
Decoding : Open an image containing the barcode then use the Barcode_Codec plugin to decode
