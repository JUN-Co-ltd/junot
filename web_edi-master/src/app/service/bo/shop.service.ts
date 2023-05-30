import { Injectable } from '@angular/core';
import { JunpcTnpmst } from 'src/app/model/junpc-tnpmst';
import { Const } from 'src/app/const/const';

@Injectable({
  providedIn: 'root'
})
export class ShopService {

  constructor() { }

  /**
   * @param stores 店舗リスト
   * @param logisticsCode 物流コード
   * @return 物流コードに該当するディスタの店舗コード
   */
  extractDistaCodeByLogisticsCode(stores: JunpcTnpmst[], logisticsCode: string): string {
    return stores
      .map(store => store.shpcd)
      .filter(shpcd => shpcd.substring(0, 4) === Const.MAKER_RETURN_SHPCD_PREFIX)
      .find(shpcd => shpcd.substring(4, 6) === logisticsCode);
  }
}
