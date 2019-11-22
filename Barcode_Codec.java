/*//////////////////////////////////////////////////////////////////
//  This file is part of Barcode_Codec plugin for ImageJ.
//
//  Barcode_Codec is free software: you can redistribute it and/or modify
//  it under the terms of the GNU General Public License as published by
//  the Free Software Foundation, either version 3 of the License, or
//  (at your option) any later version.
//
//  Barcode_Codec is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  GNU General Public License for more details.
//
//  You should have received a copy of the GNU General Public License
//  along with Barcode_Codec.  If not, see <http://www.gnu.org/licenses/>.
//
// Copyright 2016-2017 Francois GANNIER, Come PASQUALIN
/////////////////////////////////////////////////////////////////*/

import com.google.zxing.*;
import com.google.zxing.datamatrix.encoder.SymbolShapeHint;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.common.BitMatrix;
// import com.google.zxing.maxicode.MaxiCodeReader;

import ij.*;
import ij.gui.*;		// genericDialog, Roi
import ij.plugin.*; 
import ij.plugin.frame.*; 
import ij.process.ImageProcessor;
import ij.text.TextWindow;

import java.awt.*;
import java.awt.image.BufferedImage;

import java.util.EnumMap;
import java.util.Map; 
import java.awt.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.util.*;
// import java.net.URL;
import java.nio.ByteBuffer;

// ver 0.1 : initial release
// ver 0.2 : show detected barcode 
// ver 0.3 : add decoding on selection
// ver 0.4 : add toolset
// ver 1.0 : DOI release

// @SuppressWarnings("all")
@SuppressWarnings("deprecation")

public class Barcode_Codec implements PlugIn  {
	static final boolean debug = false; //true; //false;
	String sVer="1D/2D barcode Codec ver. 0.4";
	String sCop="Copyright \u00A9 2017 F.GANNIER - C.PASQUALIN";

	String getEncoder(String test) {
		String dfltChoice = "";
		int nChoice = 11;
		String[] choice0 = new String[nChoice];
		int n = 0;
		choice0[n++] = "AZTEC";
		choice0[n++] = "CODABAR";
		choice0[n++] = "CODE_39";
			// choice0[n++] = "CODE_93";
		choice0[n++] = "CODE_128";
		choice0[n++] = "DATA_MATRIX";
		choice0[n++] = "EAN_8";
		choice0[n++] = "EAN_13";
		choice0[n++] = "ITF"; // error
			// choice0[n++] = "MAXICODE";
		choice0[n++] = "PDF_417";
		choice0[n++] = "QR_CODE";
			// choice0[n++] = "RSS_14";
			// choice0[n++] = "RSS_EXPANDED";
		choice0[n++] = "UPC_A";
			// choice0[n++] = "UPC_E";
			// choice0[n++] = "UPC_EAN_EXTENSION";
	
		if (test != null) {
			for (int i=0; i<nChoice; i++)
				if (test.replace(" ","").equals(choice0[i]))
					return choice0[i];
			IJ.log("Error :" + test +". is a bad option");
		}
		dfltChoice = Prefs.get("Barcode.Encoder","QR_CODE");
		GenericDialog gd = new GenericDialog(sVer);
		gd.addChoice("BarCode Format", choice0, dfltChoice); // save last used format
		gd.addMessage(sCop);
		gd.showDialog(); 		// The macro terminates if the user clicks "Cancel"
		if (gd.wasCanceled())
			return null;
		dfltChoice = gd.getNextChoice();
		Prefs.set("Barcode.Encoder",dfltChoice);

		return dfltChoice;
	}
	
	void encode(String Format, String text2encode) {
		BitMatrix result;
		Writer writer = new MultiFormatWriter();
		int width = 256, height = 256;
		if (Format == "EAN_8" ||Format == "EAN_13" || Format == "CODABAR" || Format == "ITF")
				height = 128;
		Map<EncodeHintType, Object> hints = null;
		hints = new EnumMap<EncodeHintType, Object>(EncodeHintType.class);
		hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
			// hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.Q);
			// hints.put(EncodeHintType.MIN_SIZE, new com.google.zxing.Dimension(height, width));
		// if (Format="DATA_MATRIX")
			hints.put(EncodeHintType.DATA_MATRIX_SHAPE, SymbolShapeHint.FORCE_SQUARE);
		
		try {
			result = writer.encode(
				text2encode,
				BarcodeFormat.valueOf(Format),
				width,
				height,
				hints
			);
		} 
		catch (IllegalArgumentException iae) { 
			// Unsupported format
			IJ.log("Error : " + iae.getMessage());
			return;
		} /* */
		catch (WriterException iae) {
			IJ.log("Error : " + iae.getMessage());
			return;
		}
		
		// if (Format="DATA_MATRIX") {
		String stCM = "8-bit Black";
		int pixelsize = 0;
		if (debug) IJ.log("Original size : "+result.getWidth());
		if ( width != result.getWidth())
			pixelsize = 3; //width/result.getWidth();
			
		width = result.getWidth();
		height = result.getHeight();
		ImagePlus imp = IJ.createImage(
			"Encoded Image", 
			stCM, 
			width,
			height, 
			1
		);
		
		ImageProcessor ip = imp.getProcessor();
		byte[] pix8 = (byte[]) ip.getPixels();
		for (int y = 0; y < result.getHeight(); y++) {
			int offset = y * result.getWidth();
			for (int x = 0; x < result.getWidth(); x++) {
				pix8[offset + x] = (byte) (result.get(x, y) ? 0 : 255);
			}
		}
		if (pixelsize > 0) {
			if (debug) IJ.log("Scaling...");
			if (debug) IJ.log("Resize : " + pixelsize);
			
			int wOld = width*pixelsize;
			int hOld = height*pixelsize;
			ip = ip.resize(wOld, hOld, false);

			int wNew = width*pixelsize+16;
			int hNew = height*pixelsize+16;
			int xOff = (wNew - wOld)/2;
			int yOff = (hNew - hOld)/2;
			ImageProcessor ipNew = ip.createProcessor(wNew, hNew);
			ipNew.setColor(new Color(255,255,255));
			ipNew.fill();
			ipNew.insert(ip, xOff, yOff);
			
			new ImagePlus("Encoded Image", ipNew).show();
			
		} else 
			imp.show();
	}
	
	void decode(ImageProcessor ip) {
		Reader reader = new MultiFormatReader(); 
		// search for selection
		Rectangle r = ip.getRoi(); //r = null;
		if (debug) IJ.log("Selection : " +r.getWidth() +","+ r.getHeight()+" - "+ r.getX()+" - "+ r.getY());
		
		BufferedImage myimg = ip.crop().getBufferedImage();
		LuminanceSource source = new BufferedImageLuminanceSource(myimg);
		BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
		String resultText = null;
		BarcodeFormat bf = null;
		
		Map<DecodeHintType, Object> hints = null;
		hints = new EnumMap<DecodeHintType, Object>(DecodeHintType.class);
		hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
		// hints.put(DecodeHintType.PURE_BARCODE, Boolean.TRUE);
		try
		{
			Result result = reader.decode(bitmap, hints);
			resultText = result.getText();
			bf = result.getBarcodeFormat();
		}
			catch(NotFoundException e) { IJ.log("Error : No corresponding encoder found (" + e.getMessage()+")"); }
			catch(ChecksumException e) { IJ.log("Error : checksum error (" + e.getMessage()+")"); }
			catch(FormatException e)   { IJ.log("Error : Format error (" + e.getMessage()+")"); }
/*		
		//MAXICODE
		if (resultText==null)
		{
			if (debug) IJ.log("try MaxiCode reader!");
			reader = new MaxiCodeReader();
			try
			{
				// hints.put(DecodeHintType.PURE_BARCODE, Boolean.TRUE);
				Result result = reader.decode(bitmap, hints);
				resultText = result.getText();
			}
			catch(NotFoundException e) { IJ.log("Error : No corresponding encoder found (" + e.getMessage()+")"); }
			catch(ChecksumException e) { IJ.log("Error : MAXICODE checksum error (" + e.getMessage()+")"); }
			catch(FormatException e)   { IJ.log("Error : MAXICODE Format error (" + e.getMessage()+")"); }
		} /**/
		if (resultText!=null) {
			IJ.log(bf.toString()+" detected");
			new TextWindow("Decoded Text", resultText,300,450);
		}
		return;
	}

	public void run(String arg)	{
		String path = IJ.getDirectory("macros")+"toolsets/";
		String name = "Barcode.ijm";
		File f = new File(path+name);
		if(!f.exists()) {				// && f.isFile())
			InputStream link = (getClass().getResourceAsStream(name));
			try {
				byte[] buffer = new byte[link.available()];
				link.read(buffer);
				OutputStream outStream = new FileOutputStream(f);
				outStream.write(buffer);				
			} catch (IOException e) {
				IJ.log("error");
			}
			IJ.run("Install...", "install=["+path+name+"]");
		}				
		
		String options = Macro.getOptions();
		TextWindow win = null;
		Editor winE = null;
		String text2encode = "";
		try {
			win = (TextWindow) WindowManager.getActiveWindow();
		} 
		catch(ClassCastException ce) { }

		if (win == null) {
			try {
				winE = (Editor) WindowManager.getActiveWindow();
			} 
			catch(ClassCastException ce) { }
		} else text2encode = win.getTextPanel().getText();
		
		if (winE != null) text2encode = winE.getText();
		if (winE != null || win!= null) {
			if (text2encode == "")
				return;
			if (debug) IJ.log(text2encode);
			String Encoder = getEncoder(options);
			if (debug) IJ.log(Encoder);
			if (Encoder == null)
				return;
			encode(Encoder, text2encode);
		} else {
			ImagePlus img = WindowManager.getCurrentImage();
			if (img == null) {
				IJ.noImage(); return;
			}
			decode(img.getProcessor());
		}
		if (debug) IJ.log("Done");
	}
	
}
