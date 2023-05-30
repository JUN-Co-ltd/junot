import { HttpParameterCodec } from '@angular/common/http';

/**
 * HTTP Get 呼び出し時にURLパラメーターをURLエンコード/URLデコードする.
 *
 *  - エンコード（あ → %E3%81%82）
 *  - デコード（%E3%81%82 → あ）
 */
export class CustomEncoder implements HttpParameterCodec {
  /**
   * キーをURLエンコードする
   *
   * @param key キー
   * @returns エンコードしたキー
   */
  encodeKey(key: string): string {
    return encodeURIComponent(key);
  }

  /**
   * 値をURLエンコードする
   *
   * @param value 値
   * @returns エンコードした値
   */
  encodeValue(value: string): string {
    return encodeURIComponent(value);
  }

  /**
   * キーをURLデコードする
   *
   * @param key キー
   * @returns デコードしたキー
   */
  decodeKey(key: string): string {
    return decodeURIComponent(key);
  }

  /**
   * 値をURLデコードする
   *
   * @param value 値
   * @returns デコードした値
   */
  decodeValue(value: string): string {
    return decodeURIComponent(value);
  }
}
