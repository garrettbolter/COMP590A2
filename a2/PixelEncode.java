package a2;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Scanner;

import ac.ArithmeticEncoder;
import app.FreqCountIntegerSymbolModel;
import io.OutputStreamBitSink;

public class PixelEncode {

	public static void main(String[] args) throws IOException {
		String input_file_name = "/Users/garrettbolter/COMP590/out.dat";
		String output_file_name = "/Users/garrettbolter/COMP590/pixel_compressed.txt";

		int range_bit_width = 40;

		System.out.println("Encoding text file: " + input_file_name);
		System.out.println("Output file: " + output_file_name);
		System.out.println("Range Register Bit Width: " + range_bit_width);

		int num_symbols = (int) new File(input_file_name).length(); // should be 1228,800
		
		// Analyze file for frequency counts
		
		FileInputStream fis = new FileInputStream(input_file_name);
		// hashing value 0-256 respective pixel indeces for 1st frame
		HashMap<Integer, Integer> original_symbol_counts = new HashMap<Integer, Integer>();
		int[] symbol_counts = new int[256*2];
		int next_pixel;
		
		for (int i = 0; i < 4096; i++) {
			next_pixel = fis.read();
			original_symbol_counts.put(i, next_pixel);
			symbol_counts[next_pixel]++;	
		}
		
		next_pixel = fis.read();
		int counter = 0;
		while (next_pixel != -1) {
		int index = counter % 4096;
		int sub = original_symbol_counts.get(index) + 256;
		int count_index = sub - next_pixel;
		symbol_counts[count_index]++;
		next_pixel = fis.read();
		counter++;
		}
		
		fis.close();
		
		Integer[] symbols = new Integer[256*2];
		for (int i=0; i<symbols.length; i++) {
			symbols[i] = i;
		}

		// Create new model with analyzed frequency counts
		FreqCountIntegerSymbolModel model = new FreqCountIntegerSymbolModel(symbols, symbol_counts);

		ArithmeticEncoder<Integer> encoder = new ArithmeticEncoder<Integer>(range_bit_width);

		FileOutputStream fos = new FileOutputStream(output_file_name);
		OutputStreamBitSink bit_sink = new OutputStreamBitSink(fos);

		// First 256 * 4 bytes are the frequency counts // FOR THE ORIGINAL PICTURE
		for (int i=0; i<256*2; i++) {
			bit_sink.write(symbol_counts[i], 32);	// original_symbol_counts.get(i); ???
		}

		// Next 4 bytes are the number of symbols encoded
		bit_sink.write(num_symbols, 32);		

		// Next byte is the width of the range registers
		bit_sink.write(range_bit_width, 8);
		
		// 4096 * 4 bytes for the original frame
		
		for (int i = 0; i < 4096; i++) {
			bit_sink.write(original_symbol_counts.get(i), 32);
		}

		// Now encode the input
		fis = new FileInputStream(input_file_name);
		
		next_pixel = fis.read();
		counter = 0;
		while (next_pixel != -1) {
		int index = counter % 4096;
		int sub = original_symbol_counts.get(index) + 256;
		int count_index = sub - next_pixel;
		encoder.encode(count_index, model, bit_sink);
		next_pixel = fis.read();
		counter++;
		}
		
		fis.close();

		// Finish off by emitting the middle pattern 
		// and padding to the next word
		
		encoder.emitMiddle(bit_sink);
		bit_sink.padToWord();
		fos.close();
		
		System.out.println("Done");
	}
}
