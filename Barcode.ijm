/*//////////////////////////////////////////////////////////////////////
// Barcode.ijm
// Author: François GANNIER, Côme PASQUALIN
//
// Signalisation et Transports Ioniques Membranaires (STIM)
// CNRS ERL 7368, Groupe PCCV - Université de Tours
//
// Report bugs to authors
// gannier@univ-tours.fr
// come.pasqualin@univ-tours.fr
//
//  This file is part of Barcode_codec.
//  Copyright 2017 François GANNIER, Côme PASQUALIN
//
//  Barcode_codecis a free software: you can redistribute it and/or modify
//  it under the terms of the GNU General Public License as published by
//  the Free Software Foundation, either version 3 of the License, or
//  (at your option) any later version.
//
//  Barcode_codecis distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  GNU General Public License for more details.
//
//  You should have received a copy of the GNU General Public License
//  along with Barcode_codec.  If not, see <http://www.gnu.org/licenses/>.
////////////////////////////////////////////////////////////////// */
var sVer = "Barcode_Codec v1.0";
var sCop = "Copyright 2016-2019 F.GANNIER - C.PASQUALIN";
var pmCmds = newMenu("Popup Menu", newArray("Decode","-","About"));

// click right menu, ( ctrl click)
macro "Popup Menu" {
	cmd = getArgument( );
    if ( cmd!="-" ) run( cmd );
}

var codec = call("ij.Prefs.get", "Barcode.Encoder","DATA_MATRIX");

// id + text C800Tf207N		// C800 = red		// Txyssc	ss=size
// Dxy dot ; Lxyxy line ; Rxywh rectangle ; Fxywh filled rectangle
macro "Barcode Action Tool - C00cF001hF201hF403hF901hFc02hFf01hFh01h" {
	codec = call("ij.Prefs.get", "Barcode.Encoder","DATA_MATRIX");
	run( "Barcode Codec", codec)
}

macro "Decode" {
	testLiborExit("Barcode_Codec");
	run( "Barcode Codec" )
}

macro "Barcode Action Tool Options" {
	codec = call("ij.Prefs.get", "Barcode.Encoder","DATA_MATRIX");
	Dialog.create("Barcode option");
	items = newArray("AZTEC",  "CODABAR", "CODE_39", "CODE_128", "DATA_MATRIX", "EAN_8", "EAN_13", "ITF", "PDF_417", "QR_CODE", "UPC_A");
	Dialog.addChoice("Default codec:", items, codec);
	Dialog.show();
	codec = Dialog.getChoice();
	call("ij.Prefs.set", "Barcode.Encoder",codec);
}

macro "About" {
	exit( "<html>"
		+ "<table>"
			+"<tr>"
				+"<td>"
					+"<img src=\"http://pccv.univ-tours.fr/univtours-logo-short.png\" height=\"50\" width=\"90\" alt=\"UT\">" 
				+"</td>"
				+"<td>"
					+"<br>"
					+"<h1>"+sVer+"</h1>"
					+"<br>"
				+"</td>"
			+"</tr>"
			+"</table>"
		+"<ul>"
		+"<li> Default encoder : "+codec
		+"<li>more information at <a href=http://pccv.univ-tours.fr/ImageJ>http://pccv.univ-tours.fr/ImageJ</a>"
		+"</ul>"
		+"<p>"+sCop
		+"<p>"
	);
}

function testLib(lib) {
	if(indexOf(eval("script","Class.forName(\""+lib+"\")"),"ClassNotFoundException")!=-1) return true; else return false;
}

function testLiborExitI(lib,info) {
	if (!testLib(lib))
		exit("<html>"
			+"<h1>Spiky</h1>"
			+"<u>Error</u>: "+lib+" class not found"
			+"<ul>"
			+"<li>tip: "+info+"</b>"
			+"</ul>");
}

function testLiborExit(lib) {
	testLiborExitI(lib,"download and install "+lib+".jar");
}
