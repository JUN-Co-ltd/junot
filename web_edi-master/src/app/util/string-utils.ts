import { ObjectUtils } from './object-utils';

/**
 * 文字列ユーティリティ.
 */
export class StringUtils {
  constructor(
  ) { }

  /**
   * strが空（''）、またはnull、またはundefinedの場合、defaultStrの値を返します。
   *
   * - StringUtils.defaultIfEmpty(null, 'NULL')      = 'NULL'
   * - StringUtils.defaultIfEmpty('', 'NULL')        = 'NULL'
   * - StringUtils.defaultIfEmpty(undefined, 'NULL') = 'NULL'
   * - StringUtils.defaultIfEmpty(' ', 'NULL')       = ' '
   * - StringUtils.defaultIfEmpty('bat', 'NULL')     = 'bat'
   * - StringUtils.defaultIfEmpty('', null)          = null
   *
   * @param str チェックする文字列
   * @param defaultStr strが空（''）、またはnull、またはundefinedの場合の戻り値
   * @returns str または、defaultStr
   */
  static defaultIfEmpty(str: string, defaultStr: string): string {
    if (StringUtils.isEmpty(str)) {
      return defaultStr;
    }
    return str.toString();
  }

  /**
   * strが空（''）または、null、またはundefinedかをチェックします。
   *
   * - StringUtils.isEmpty(null)      = true
   * - StringUtils.isEmpty('')        = true
   * - StringUtils.isEmpty(undefined) = true
   * - StringUtils.isEmpty(' ')       = false
   * - StringUtils.isEmpty('bob')     = false
   * - StringUtils.isEmpty('  bob  ') = false
   *
   * @param str チェックする文字列
   * @returns
   * - true : strが空またはnullまたはundefinedの場合
   */
  static isEmpty(str: string): boolean {
    return str == null || str === undefined || str.length === 0;
  }

  /**
   * strが空（''）ではない、かつnullではない、かつundefinedではないかをチェックします。
   *
   * - StringUtils.isNotEmpty(null)      = false
   * - StringUtils.isNotEmpty('')        = false
   * - StringUtils.isNotEmpty(undefined) = false
   * - StringUtils.isNotEmpty(' ')       = true
   * - StringUtils.isNotEmpty('bob')     = true
   * - StringUtils.isNotEmpty('  bob  ') = true
   *
   * @param str チェックする文字列
   * @returns
   * - true : strが空、null、undefinedのいずれでもない場合
   */
  static isNotEmpty(str: string): boolean {
    return !StringUtils.isEmpty(str);
  }

  /**
   * valueが空白または、null、またはundefinedかをチェックします。
   *
   * - StringUtils.isBlank(null)         = true
   * - StringUtils.isBlank(undefined)    = true
   * - StringUtils.isBlank('')           = true
   * - StringUtils.isBlank(' ')          = true
   * - StringUtils.isBlank('  ')         = true
   * - StringUtils.isBlank('　　')       = true
   * - StringUtils.isBlank('\n\n')       = true
   * - StringUtils.isBlank('\r\r')       = true
   * - StringUtils.isBlank('\t\t')       = true
   * - StringUtils.isBlank('ab de fg')   = false
   * - StringUtils.isBlank('ab  de  fg') = false
   * - StringUtils.isBlank(' a ')        = false
   *
   * @param value チェックする文字列
   * @returns
   * - true : valueが空白またはnullまたはundefinedの場合
   */
  static isBlank(value: string): boolean {
    if (this.isEmpty(value)) { return true; }

    return this.replaceWhitespace(value, '').length === 0;
  }

  /**
   * valueが空白または、null、またはundefined以外かをチェックします。
   *
   * - StringUtils.isNotBlank(null)         = false
   * - StringUtils.isNotBlank(undefined)    = false
   * - StringUtils.isNotBlank('')           = false
   * - StringUtils.isNotBlank(' ')          = false
   * - StringUtils.isNotBlank('  ')         = false
   * - StringUtils.isNotBlank('　　')       = false
   * - StringUtils.isNotBlank('\n\n')       = false
   * - StringUtils.isNotBlank('\r\r')       = false
   * - StringUtils.isNotBlank('\t\t')       = false
   * - StringUtils.isNotBlank('ab de fg')   = true
   * - StringUtils.isNotBlank('ab  de  fg') = true
   * - StringUtils.isNotBlank(' a ')        = true
   *
   * @param value チェックする文字列
   * @returns
   * - true : valueが空白またはnullまたはundefinedの場合
   */
  static isNotBlank(value: string): boolean {
    return !this.isBlank(value);
  }

  /**
   * string型へ変換します。値がnullまたはundefinedの場合はそのまま返します。
   *
   * - StringUtils.toStringSafe(null)      = null
   * - StringUtils.toStringSafe(undefined) = undefined
   *
   * @param value string変換する値
   * @returns string型に変換した値
   */
  static toStringSafe(value: any): string {
    if (!value) { return value; }
    return value.toString();
  }

  /**
   * valueにある空白をreplaceValueで置換する。
   *
   * - StringUtils.replaceWhitespace(null, null)            = null
   * - StringUtils.replaceWhitespace(undefined, undefined)  = undefined
   * - StringUtils.replaceWhitespace('ab de fg', null)      = 'ab de fg'
   * - StringUtils.replaceWhitespace('ab de fg', undefined) = 'ab de fg'
   * - StringUtils.replaceWhitespace('', '')                = ''
   * - StringUtils.replaceWhitespace(' ', 'a')              = 'a'
   * - StringUtils.replaceWhitespace('ab de fg', '')        = 'abdefg'
   * - StringUtils.replaceWhitespace('ab  de  fg', ' ')     = 'ab de fg'
   * - StringUtils.replaceWhitespace('ab  de  fg', 'z')     = 'abzdezfg'
   * - StringUtils.replaceWhitespace('ab　de　fg', ' ')     = 'ab de fg'
   * - StringUtils.replaceWhitespace('ab\nde\nfg', ' ')     = 'ab de fg'
   * - StringUtils.replaceWhitespace('ab\rde\rfg', ' ')     = 'ab de fg'
   * - StringUtils.replaceWhitespace('ab\tde\tfg', ' ')     = 'ab de fg'
   *
   * 空白の種類
   *
   * - 半角スペース
   * - 全角スペース
   * - タブ（\t）
   * - 改行（\n, \r）
   *
   * @param value 置換元の文字列
   * @param replaceValue 置換後の文字列
   * @returns 置換した文字列
   */
  static replaceWhitespace(value: string, replaceValue: string): string {
    if ((this.isEmpty(value)) || ObjectUtils.isNullOrUndefined(replaceValue)) { return value; }

    return value.replace(/[\x20\u3000\t\r\n]+/g, replaceValue).trim();
  }

  /**
   * valueを空白で分割する。
   *
   * - StringUtils.splitByWhitespace(null)         = null
   * - StringUtils.splitByWhitespace(undefined)    = undefined
   * - StringUtils.splitByWhitespace('')           = []
   * - StringUtils.splitByWhitespace(' ')          = []
   * - StringUtils.splitByWhitespace('ab de fg')   = ['ab', 'de', 'fg']
   * - StringUtils.splitByWhitespace('ab   de fg') = ['ab', 'de', 'fg']
   * - StringUtils.splitByWhitespace(' ab de fg ') = ['ab', 'de', 'fg']
   * - StringUtils.splitByWhitespace('ab　de　fg') = ['ab', 'de', 'fg']
   * - StringUtils.splitByWhitespace('ab\nde\nfg') = ['ab', 'de', 'fg']
   * - StringUtils.splitByWhitespace('ab\rde\rfg') = ['ab', 'de', 'fg']
   * - StringUtils.splitByWhitespace('ab\tde\tfg') = ['ab', 'de', 'fg']
   *
   * @param value 分割する文字列
   * @returns 分割した文字列
   */
  static splitByWhitespace(value: string): string[] {
    if (ObjectUtils.isNull(value)) { return null;
    } else if (ObjectUtils.isUndefined(value)) { return undefined; }

    const str = this.replaceWhitespace(value, ' ').trim();

    if (this.isEmpty(str)) { return []; }

    return str.split(' ');
  }

  /**
   * valueから空白を削除する。
   *
   * - StringUtils.deleteWhitespace(null)         = null
   * - StringUtils.deleteWhitespace(undefined)    = undefined
   * - StringUtils.deleteWhitespace('')           = ''
   * - StringUtils.deleteWhitespace(' ')          = ''
   * - StringUtils.deleteWhitespace('ab de fg')   = 'abdefg'
   * - StringUtils.deleteWhitespace('ab   de fg') = 'abdefg'
   * - StringUtils.deleteWhitespace(' ab de fg ') = 'abdefg'
   * - StringUtils.deleteWhitespace('ab　de　fg') = 'abdefg'
   * - StringUtils.deleteWhitespace('ab\nde\nfg') = 'abdefg'
   * - StringUtils.deleteWhitespace('ab\rde\rfg') = 'abdefg'
   * - StringUtils.deleteWhitespace('ab\tde\tfg') = 'abdefg'
   *
   * @param value 削除元の文字列
   * @returns 削除した文字列
   */
  static deleteWhitespace(value: string): string {
    if (this.isEmpty(value)) { return value; }

    return this.replaceWhitespace(value, '');
  }

  /**
   * valueから先頭と末尾の空白文字と行終端文字を削除する。
   *
   * - StringUtils.trim(null)          = null
   * - StringUtils.trim(undefined)     = undefined
   * - StringUtils.trim('')            = ''
   * - StringUtils.trim('     ')       = ''
   * - StringUtils.trim('abc')         = 'abc'
   * - StringUtils.trim('    abc    ') = 'abc'
   *
   * @param value 文字列
   * @returns トリム後の値
   */
  static trim(value: string): string {
    if (StringUtils.isEmpty(value)) {
      return value;
    }

    return value.trim();
  }

  /**
   * valueから先頭と末尾の空白文字と行終端文字を削除する。
   * 削除後の文字列が空（''）、またはnull、またはundefinedの場合、nullを返却する。
   *
   * - StringUtils.trimToNull(null)          = null
   * - StringUtils.trimToNull(undefined)     = null
   * - StringUtils.trimToNull('')            = null
   * - StringUtils.trimToNull('     ')       = null
   * - StringUtils.trimToNull('abc')         = 'abc'
   * - StringUtils.trimToNull('    abc    ') = 'abc'
   *
   * @param value 文字列
   * @returns トリム後の値
   */
  static trimToNull(value: string): string {
    const ts = this.trim(value);

    if (StringUtils.isEmpty(ts)) {
      return null;
    }

    return ts;
  }

  /**
   * valueから先頭と末尾の空白文字と行終端文字を削除する。
   * 削除後の文字列が空（''）、またはnull、またはundefinedの場合、空（''）を返却する。
   *
   * - StringUtils.trimToEmpty(null)          = ''
   * - StringUtils.trimToEmpty(undefined)     = ''
   * - StringUtils.trimToEmpty('')            = ''
   * - StringUtils.trimToEmpty('     ')       = ''
   * - StringUtils.trimToEmpty('abc')         = 'abc'
   * - StringUtils.trimToEmpty('    abc    ') = 'abc'
   *
   * @param value 文字列
   * @returns トリム後の値
   */
  static trimToEmpty(value: string): string {
    if (StringUtils.isEmpty(value)) {
      return '';
    }

    return value.trim();
  }
}
