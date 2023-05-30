/**
 * ファイル操作　ユーティリティ.
 */
export class FileUtils {
  constructor(
  ) { }

  /**
   * ファイルをダウンロードする。
   * @param blob ファイルの実体
   * @param fileName ファイル名
   */
  static downloadFile(blob: Blob, fileName: string): void {
    if (window.navigator.msSaveOrOpenBlob) {
      // IEの場合
      navigator.msSaveBlob(blob, fileName);
      return;
    }

    // IE以外(Chrome, Firefox)
    const link = document.createElement('a');
    link.setAttribute('href', window.URL.createObjectURL(blob));

    link.setAttribute('download', fileName);
    document.body.appendChild(link);

    link.click();
    link.remove();
  }
}
