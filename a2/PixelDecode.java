package a2;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Scanner;

import ac.ArithmeticDecoder;
import app.FreqCountIntegerSymbolModel;
import io.InputStreamBitSource;
import io.InsufficientBitsLeftException;

public class PixelDecode {

	public static void main(String[] args) throws InsufficientBitsLeftException, IOException {
		String input_file_name = "/Users/garrettbolter/COMP590/pixel_compressed.txt";
		String output_file_name = "/Users/garrettbolter/COMP590/pixel_uncompressed.txt";

		FileInputStream fis = new FileInputStream(input_file_name);

		InputStreamBitSource bit_source = new InputStreamBitSource(fis);

		// Read in symbol counts and set up model
		
		int[] symbol_counts = new int[256*2];
		Integer[] symbols = new Integer[256*2];
		
		for (int i=0; i<256*2; i++) {
			symbol_counts[i] = bit_source.next(32);
			symbols[i] = i;
		}

		FreqCountIntegerSymbolModel model = new FreqCountIntegerSymbolModel(symbols, symbol_counts);
		
		// Read in number of symbols encoded

		int num_symbols = bit_source.next(32);

		// Read in range bit width and setup the decoder

		int range_bit_width = bit_source.next(8);
		ArithmeticDecoder<Integer> decoder = new ArithmeticDecoder<Integer>(range_bit_width);

		int[] original_values = new int[4096];
		
		for (int i = 0; i < 4096; i++) {
			original_values[i] = bit_source.next(32);
		}
		
		// Decode and produce output.
		
		System.out.println("Uncompressing file: " + input_file_name);
		System.out.println("Output file: " + output_file_name);
		System.out.println("Range Register Bit Width: " + range_bit_width);
		System.out.println("Number of symbols: " + num_symbols);
		
		FileOutputStream fos = new FileOutputStream(output_file_name);
		int count = 0;
		for (int i=0; i<num_symbols; i++) {
			int sym = decoder.decode(model, bit_source);
			int index = count % 4096;
			sym = original_values[index] - 256 + sym;
			fos.write(sym);
			count++;
		}

		System.out.println("Done.");
		fos.flush();
		fos.close();
		fis.close();
	}
}

