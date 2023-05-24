// CHECKSTYLE:OFF
package test.utils;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

public class FileController {
    /**
     * filePathで指定したファイルを読み込み内容をStringで返す
     * @param filePath ファイル絶対パス
     * @param encoding エンコード
     * @return ファイルの内容
     * @throws IOException
     */
    public static String readFile(final String filePath, final String encoding) throws IOException {
        return FileUtils.readFileToString(new File(filePath), encoding);
    }

    /**
     * filePathで指定したファイルを読み込み内容をStringで返す.
     * ファイルの文字コードはUTF8.
     * @param filePath ファイル絶対パス
     * @return ファイルの内容
     * @throws IOException
     */
    public static String readFileUTF8(final String filePath) throws IOException {
        return readFile(filePath, "UTF-8");
    }

    /**
     * filePathで指定したファイルを読み込み内容をStringで返す.
     * ファイルの文字コードはSJIS
     * @param filePath ファイル絶対パス
     * @return ファイルの内容
     * @throws IOException
     */
    public static String readFileSJIS(final String filePath) throws IOException {
        return readFile(filePath, "Shift-JIS");
    }

    /**
     * ファイルへ出力する.
     * @param filePath 出力先ファイル絶対パス
     * @param data ファイルの内容
     * @param encoding エンコード
     * @throws IOException 例外
     */
    public static void writeFile(String filePath, String data, String encoding) throws IOException {
        FileUtils.writeStringToFile(new File(filePath), data, encoding);
    }

    /**
     * ファイルへ出力する.
     * ファイルの文字コードはUTF8.
     * @param filePath 出力先ファイル絶対パス
     * @param data ファイルの内容
     * @throws IOException
     */
    public static void writeFileUTF8(String filePath, String data) throws IOException {
        FileUtils.writeStringToFile(new File(filePath), data, "UTF-8");
    }

    /**
     * ファイルへ出力する.
     * ファイルの文字コードはUTF8.
     * @param filePath 出力先ファイル絶対パス
     * @param data ファイルの内容
     * @throws IOException
     */
    public static void writeFileSJIS(String filePath, String data) throws IOException {
        FileUtils.writeStringToFile(new File(filePath), data, "Shift-JIS");
    }
}
//CHECKSTYLE:ON
