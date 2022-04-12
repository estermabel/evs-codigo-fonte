package br.ucsal.softevo.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.opencsv.CSVWriter;

public class Main {
	final public static String LINES_OF_CODE_REGEX = "^/\\\\b([a-z0-9]+)\\\\b(?<!//|\\\\|\\\\*?|\\\\*)";
	final public static String CLASS_REGEX = "class";
	final public static String METHOD_REGEX = "(?:(?:public|private|protected|static|final|native|synchronized|abstract|transient)+\\s+)+[$_\\w<>\\[\\]\\s]*\\s+[\\$_\\w]+\\([^\\)]*\\)?\\s*\\{?[^\\}]*\\}?";
	final public static String CONSTRUCTOR_REGEX = "(private|protected|internal|public) [A-Za-z0-9_]+[(]";
	final public static String DUPLICATED_METHOD_REGEX = "(\\(+?)([^\\=;]*?)(\\{+?)";
	
	final public static List<String> FILE_NAMES = new ArrayList<String>();
	
	public static FileWriter outputFile;

	public static int numbersOfClasses = 0;
	public static int numbersOfLinesOfCode = 0;
	public static int numbersOfMethods = 0;

	public static void main(String[] args) throws IOException {
		getInfo();
	}

	public static void getInfo() throws IOException {
		int maxMonth = 27;
		setupFileNameList();
		for (int monthNumber = 1; monthNumber <= maxMonth; monthNumber++) {
			for (String file : FILE_NAMES) {
				getCodeAnalysisResult(getUrlContentReader(getPath(monthNumber, file)));
			}
			prinResult(monthNumber);
		}
		List<String> resultSet = Arrays.asList(String.valueOf(numbersOfLinesOfCode), String.valueOf(numbersOfMethods), String.valueOf(numbersOfClasses));
		writeCsv(resultSet);
	}

	public static String getPath(int monthNumber, String fileName) {

		return "https://raw.githubusercontent.com/estermabel/evs-dataset-codigo-fonte/main/Dataset/" + monthNumber + "/"
				+ fileName;
	}

	public static List<String> setupFileNameList() {
		FILE_NAMES.add("DispatchQueue.txt");
		FILE_NAMES.add("FileLoader.txt");
		FILE_NAMES.add("FileLog.txt");
		FILE_NAMES.add("FileUploadOperation.txt");
		FILE_NAMES.add("UserConfig.txt");
		FILE_NAMES.add("Utilities.txt");
		return FILE_NAMES;
	}

	public static void getCodeAnalysisResult(BufferedReader bufferedReader) throws IOException {
		try {
			String line;
			numbersOfClasses = 0;
			numbersOfLinesOfCode = 0;
			numbersOfMethods = 0;
			String textOverride = "@Override";
			List<String> listMethods = new LinkedList<>();
			while ((line = bufferedReader.readLine()) != null) {
				if (!checkComments(line)) {
					if (!doesLineMatchesRegex(line, LINES_OF_CODE_REGEX)) {
						numbersOfLinesOfCode++;
					}
				}
				if (line.contains(textOverride)) {
					listMethods.remove(line);
					numbersOfMethods--;
				} else if (doesLineMatchesRegex(line, METHOD_REGEX) || doesLineMatchesRegex(line, CONSTRUCTOR_REGEX) ) {
					numbersOfMethods++;
				}

				if (doesLineMatchesRegex(line, CLASS_REGEX)) {
					numbersOfClasses++;
				}
			}
			bufferedReader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static BufferedReader getUrlContentReader(String path) {
		URL url;
		URLConnection urlConnection;
		BufferedReader bufferedReader = null;
		try {
			url = new URL(path);
			urlConnection = url.openConnection();
			bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return bufferedReader;
	}

	public static Boolean doesLineMatchesRegex(String line, String regex) {
		Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(line);
		return matcher.find();
	}

	public static Boolean checkComments(String line) {
		String singleComment = "//";
		String miltiLineCommentBody1 = " *";
		String miltiLineCommentBody2 = "*";
		String multiLineCommentStart = "/*";
		String multiLineCommentEnd = "*/";

		return ((line.contains(singleComment) || line.startsWith(miltiLineCommentBody1)
				|| line.startsWith(miltiLineCommentBody2) || line.contains(multiLineCommentStart)
				|| line.contains(multiLineCommentEnd) || line.isEmpty()));

	}

	public static void prinResult(int monthNumber) {
		String result = "Mês " + monthNumber + ":\nLOC -> " + numbersOfLinesOfCode + "\nMetodos totais -> " + numbersOfMethods
				+ "\nClasses -> " + numbersOfClasses + "\n\n";
		System.out.println(result);
	}
	
	public static void writeCsv(List<String> resultSet) throws IOException {
		outputFile = new FileWriter(new File(System.getProperty("user.dir") + "text.csv"));
		CSVWriter writer = new CSVWriter(outputFile, ';',
	            CSVWriter.NO_QUOTE_CHARACTER,
	            CSVWriter.DEFAULT_ESCAPE_CHARACTER,
	            CSVWriter.DEFAULT_LINE_END);
		List<String[]> resultToWrite = new ArrayList<String[]>();
		resultSet.forEach(e -> {
			resultToWrite.add(e.split(e));
		});
		String[] header = { "Linhas de código", "Metodos totais ", "Classes totais" };
		writer.writeNext(header);
		String[] data = {"teste", "teste2", "teste3"}; 
		writer.writeNext(data);
		writer.close();
	}
}