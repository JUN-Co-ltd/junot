import { CodeMaster } from '../const/code-master';
import { PartNoKind } from '../interface/part-no-kind';
import { PartNo } from '../interface/part-no';

/**
 * ビジネスロジック　ユーティリティ.
 */
export class BusinessUtils {
  constructor(
  ) { }

  /**
   * サブシーズンコード名を取得する.
   * @param subSeasonId サブシーズンID
   * @returns サブシーズンコード名
   */
  public static getSeasonValue(subSeasonId: string): string {
    const target = CodeMaster.subSeason.find(codeItem => codeItem.id === Number(subSeasonId));
    return target != null ? target.value : null;
  }

  /**
   * 品番を品種と通番に分割して返す。
   * @param partNo 品番
   * @returns 分割した品番
   */
  public static splitPartNo(partNo: string): PartNo {
    const splitPartNo: PartNo = { partNoKind: null, partNoSerialNo: null };
    if (partNo == null) { return splitPartNo; }
    switch (partNo.length) {
      case 5: // 品番が5桁の場合は品種なし、通番のみ
        splitPartNo.partNoSerialNo = partNo;
        break;
      case 2:
      case 3:
        // 品番が2桁または3桁の場合は通番なし、品種のみ
        splitPartNo.partNoKind = partNo;
        break;
      case 7: // 品番が7桁の場合は品種2桁
        splitPartNo.partNoKind = partNo.slice(0, 2);
        splitPartNo.partNoSerialNo = partNo.slice(2);
        break;
      case 8: // 品番が8桁の場合は品種3桁
        splitPartNo.partNoKind = partNo.slice(0, 3);
        splitPartNo.partNoSerialNo = partNo.slice(3);
        break;
      default:
        break;
    }
    return splitPartNo;
  }

  /**
   * 品種をブランドコードとアイテムコードに分割して返す。
   * @param partNoKind 品種
   * @returns 分割した品種
   */
  public static splitPartNoKind(partNoKind: string): PartNoKind {
    const splitPartNoKind: PartNoKind = { blandCode: null, itemCode: null };
    if (partNoKind == null) { return splitPartNoKind; }
    switch (partNoKind.length) {
      case 2: // 品種が2桁の場合はアイテムコードなし
        splitPartNoKind.blandCode = partNoKind;
        break;
      case 3:
        splitPartNoKind.blandCode = partNoKind.slice(0, 2);
        splitPartNoKind.itemCode = partNoKind.slice(2);
        break;
      default:
        break;
    }
    return splitPartNoKind;
  }
}
