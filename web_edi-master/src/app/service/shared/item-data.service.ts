import { Injectable } from '@angular/core';
import { BehaviorSubject, Subject } from 'rxjs';

import { Sku } from '../../model/sku';
import { JunpcSizmst } from '../../model/junpc-sizmst';

/**
 * itemのComponent間データ共有Service
 */
@Injectable()
export class ItemDataService {
  constructor() { }

  /** SKUの色が変更された時に呼び出す */
  public changeColor$ = new Subject<Sku>();

  /** SKUのサイズが変更された時に呼び出す */
  public changeSize$ = new Subject<Sku>();

  /** SKUフォームが作成完了時に呼び出す */
  public isCreateSkus$ = new BehaviorSubject<boolean>(false);
  /** 品番画面で取得したサイズマスタ */
  public sizeMasterList: JunpcSizmst[] = [];

  /** 初期表示またはJAN区分が変更されたときに呼び出す */
  public janType$ = new BehaviorSubject<number>(null);
}
