import { Component, OnInit, Input } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { FukukitaruMasterType, CompositionsCommon } from '../../const/const';
import { TabModel } from '../../model/tab-model';
import { FukukitaruMaterialAttentionName } from '../../model/fukukitaru-material-attention-name';
import { FukukitaruMaterialAttentionTag } from '../../model/fukukitaru-material-attention-tag';
import { FukukitaruAttentionByColor } from '../../interface/fukukitaru-attention-by-color';
import { MaterialFileInfo } from '../../model/material-file-info';
import { FukukitaruOrderSku } from '../../model/fukukitaru-order-sku';

import { FileService } from '../../service/file.service';
import { SwitchTabService } from '../../service/switch-tab.service';

import { FileUtils } from '../../util/file-utils';
import { NumberUtils } from 'src/app/util/number-utils';

class ResultItem {
  master: FukukitaruMaterialAttentionName;
  item: FukukitaruOrderSku;
  selected: boolean;
}

@Component({
  selector: 'app-attention-modal',
  templateUrl: './attention-modal.component.html',
  styleUrls: ['./attention-modal.component.scss']
})
export class AttentionModalComponent implements OnInit {
  readonly FUKUKITARU_MASTER_TYPE = FukukitaruMasterType;
  /** 選択可能な個数 */
  readonly MAX_SELECT_COUNT: number = 3;
  readonly EXCEEDED_MAX_SELECT_COUNT: number = 4;

  @Input() type: FukukitaruMasterType;
  @Input() attentionNameList: FukukitaruMaterialAttentionName[] = [];
  @Input() attentionTagList: FukukitaruMaterialAttentionTag[] = [];
  @Input() saveList: FukukitaruAttentionByColor[] = [];
  @Input() materialFileList: MaterialFileInfo[] = [];  // 資材発注ファイルリスト

  title: string;
  overallErrorMsgCode: string;
  tabs: TabModel[] = new Array<TabModel>();
  /** 表示するカラーのコード保持用 */
  selectedColorCode: string;
  /** 表示するタブ */
  currentTab: any;
  /** 選択中の行 */
  selectedItems: FukukitaruAttentionByColor[] = [];
  /** 検索結果 */
  resultItems: ResultItem[] = [];
  list: FukukitaruMaterialAttentionName[] | FukukitaruMaterialAttentionTag[] = [];
  /** 00共通と一致する色別のアテンション */
  sameCommonAttention: FukukitaruAttentionByColor[] = [];

  constructor(
    public activeModal: NgbActiveModal,
    private fileService: FileService,
    private switchTabService: SwitchTabService,
  ) { }

  ngOnInit() {
    if (this.type === FukukitaruMasterType.ATTENTION_TAG) {
      this.title = 'アテンションタグ' ;
      this.list = this.attentionTagList;
    } else {
      this.title = 'アテンションネーム';
      this.list = this.attentionNameList;
    }

    // 資材数量をnumber型に変換する
    this.convertOrderLotToNumber();

    this.sameCommonAttention = [];
    this.overallErrorMsgCode = '';
    this.setInitTab();

    // データ表示
    this.display();
  }

  /**
   * 資材数量をnumber型に変換する.
   * ※不適切文字列や空は変換しない
   */
  private convertOrderLotToNumber(): void {
    this.saveList.forEach(attentions => {
      attentions.attentionList.forEach(attention => {
        if (NumberUtils.isNumber(attention.orderLot)) {
          attention.orderLot = Number(attention.orderLot);
        }
      });
    });
  }

  /**
   *  タブの初期表示処理を行う。
   */
  private setInitTab(): void {
    // タブを設定する
    this.saveList.forEach(saveVal => {
      this.tabs.push(new TabModel(saveVal.colorCode + ' ' + saveVal.colorName, null, false));
    });

    this.tabs[0].current = true;

    // 選択しているタブのカラーのみのデータを表示するため、カラーコードを取得しておく
    const selectedTab = this.tabs.filter(tab => tab.current === true);
    this.selectedColorCode = selectedTab[0].name.slice(0, 2);

    this.switchTabService.setTabContentList(this.tabs);

    // 選択中のタブを表示する。
    this.currentTab = this.switchTabService.getCurrentContents();
  }

  /**
   * 登録データを画面に表示します。
   * - 00共通と選択が同じものは00共通で選択。色別には選択されない
   * - materialIdとsortOrderの組み合わせが完全一致する色のattentionListをクリアして表示
   */
  private display(): void {
    // 00共通のアテンションネーム/タグ
    const commonColorAttention = this.saveList.find(color1 =>
      color1.colorCode === CompositionsCommon.COLOR_CODE && color1.attentionList.length > 0);

    // 00共通と一致するアテンションネーム/タグを抽出(00共通含まない)
    if (commonColorAttention != null) {
      this.saveList
        .filter(attention1 => attention1.colorCode !== CompositionsCommon.COLOR_CODE)
        .forEach(attention2 => {
          if (attention2.attentionList.length !== commonColorAttention.attentionList.length) {
            // 長さが違う場合は不一致。以降処理しない
            return;
          }
          // 長さが同じ場合、materialIdとsortOrderの組み合わせが完全一致するか
          const isAllInclude = commonColorAttention.attentionList.every(commonAttention =>
            (attention2.attentionList.some(attention3 =>
              attention3.materialId === commonAttention.materialId && attention3.sortOrder === commonAttention.sortOrder)));
          if (isAllInclude) {
            // 完全一致する場合、 アテンションネーム/タグは00共通と同じ。sameCommonAttentionに保持する
            this.sameCommonAttention.push(JSON.parse(JSON.stringify(attention2)));
          }
        });
    }
    const sameCommonAttentionColor = this.sameCommonAttention.map(attention => attention.colorCode);
    // 登録データよりアテンションネーム/タグと同じ色のattentionListをクリア
    // ※色別で選択されないよう表示するため。onSelect()時にクリアしたデータ再セット
    if (sameCommonAttentionColor.length !== 0) {
      this.saveList
      .filter(attention => sameCommonAttentionColor.includes(attention.colorCode))
      .forEach(attention => attention.attentionList.length = 0);
    }

    // 全カラーのデータを保持
    this.selectedItems = JSON.parse(JSON.stringify(this.saveList));

    // 選択したカラーのみデータを抽出
    const selectedAppendicesTermByColor = this.selectedItems.filter(
      appendicesTerm => appendicesTerm.colorCode === this.selectedColorCode);

    // 選択したカラーのみ画面表示用に変換
    this.resultItems = this.list.map(mstData => this.toAttetion(mstData, selectedAppendicesTermByColor[0]));
  }

  /**
   * タブがクリックされた時のイベントハンドラ
   * @param tabName 選択したタブ名
   */
  onTabClick(tabName: string): void {
    this.selectedColorCode = tabName.slice(0, 2);  // 選択したタブのカラーを保持する

    // クリックされたタブに切り替える
    this.changeCurrent(tabName);

    // 選択したカラーのみデータを抽出
    const selectedAttentionByColor = this.selectedItems
      .filter(selectedAttention => selectedAttention.colorCode === this.selectedColorCode);

    // 選択したカラーのみ画面表示用に変換
    this.resultItems = this.list.map(mstData => this.toAttetion(mstData, selectedAttentionByColor[0]));
  }

  /**
   * タブのみ切り替える
   * @param name クリックされたタブのタブ名
   */
  public changeCurrent(name: string): any {
    for (const target in this.tabs) {
      if (this.tabs.hasOwnProperty(target)) {
        this.tabs[target].current = false;

        if (this.tabs[target].name === name) {
          this.tabs[target].current = true;
        }
      }
    }
  }

  /**
   * マスタデータを画面表示用に変換します。
   * @param mstData マスタデータ
   * @return 検索結果に表示する結果
   */
  private toAttetion(fukukitaruMaster: FukukitaruMaterialAttentionName,
    selectedAttentionByColor: FukukitaruAttentionByColor): ResultItem {
    let selected = false;
    let selectedItem: FukukitaruOrderSku = null;

    // materialIdはマスタデータのidと紐づいている
    const selectedItems = selectedAttentionByColor.attentionList
      .filter(selectedAttention => selectedAttention.materialId === fukukitaruMaster.id);

    if (selectedItems.length >= 1) {
      selected = true;
      selectedItem = selectedItems[0];
    } else {
      selectedItem = {
        /** フクキタル発注SKU情報ID. */
        id: null,
        /** フクキタル発注ID. */
        fOrderId: null,
        /** カラーコード. */
        colorCode: selectedAttentionByColor.colorCode,
        /** サイズ. */
        size: '',
        /** 資材ID. */
        materialId: fukukitaruMaster.id,
        /** 資材数量. */
        orderLot: 0,
        /** 資材種類. */
        materialType: fukukitaruMaster.materialType,
        /** 資材種類名. */
        materialTypeName: fukukitaruMaster.materialTypeName,
        /** 資材コード. */
        materialCode: fukukitaruMaster.materialCode,
        /** 資材コード名. */
        materialCodeName: fukukitaruMaster.materialCodeName,
        /** 並び順. */
        sortOrder: null,
        /** 出荷単位.(画面表示用) */
        moq: null
      };
    }

    return {
      master: fukukitaruMaster,
      item: selectedItem,
      selected: selected
    } as ResultItem;
  }

  /**
   * 選択ボタンクリック時、親ページに選択行のデータを渡して、モーダルを閉じます。
   */
  onSelect(): void {
    const result = this.emptySortOrderItems();
    // 整形したデータを返却
    this.activeModal.close(this.formatResult(result));
  }

  /**
   * アテンションの整形
   * - display()時にアテンションネーム/タグをクリアした色のデータの再セット
   * - 色別タブで選択がある場合は色別のattentionListを新たに作成
   * - 色別タブで選択がない場合は00共通と同じ選択とみなす。
   *   00共通と同じデータをセット(materialIdとsortOrderがクリアしたデータと一致する場合はクリアしたデータをセット)。
   * @param result 現時点のアテンションネーム/タグリスト(00共通含む)
   */
  private formatResult(result: FukukitaruAttentionByColor[]): FukukitaruAttentionByColor[] {
    const formattedResults: FukukitaruAttentionByColor[] = result.concat();

    // 00共通のアテンションネーム/タグ
    const commonColorAttention = result.find(color1 =>
      color1.colorCode === CompositionsCommon.COLOR_CODE && color1.attentionList.length > 0);

    if (commonColorAttention == null) {
      // 00共通のアテンションが選択されていない場合は、以降処理しない
      return formattedResults;
    }

    // 00共通のアテンションが選択されている場合、選択がない色のattentionListデータ再セット
    formattedResults
      .filter(color2 => color2.attentionList.length === 0)
      .forEach(color3 => {
        // display()時にアテンションネーム/タグをクリアした色のデータ
        const preSameCommonAttention = this.sameCommonAttention.find(preAttention =>
          preAttention.colorCode === color3.colorCode);

        if (preSameCommonAttention == null) {
          // preSameCommonAttentionがない場合は、現在の00共通で選択したアテンションネーム/タグを新たに色別にセット
          commonColorAttention.attentionList.forEach(commonAttention => {
            const newAttention = this.createAttentionValue(commonAttention, color3.colorCode);
            color3.attentionList.push(newAttention);
          });
        } else {
          // preSameCommonAttentionがある場合
          commonColorAttention.attentionList.forEach(commonAttention => {
            // materialIdとsortOrderが一致するものを抽出
            const preMaterial = preSameCommonAttention.attentionList.find(attenton =>
              attenton.materialId === commonAttention.materialId && attenton.sortOrder === commonAttention.sortOrder);
            if (preMaterial != null) {
              // 一致するものがある場合はアテンションネーム/タグクリア前のデータをセット
              color3.attentionList.push(preMaterial);
            } else {
              // 一致するものがない場合は00共通と同じアテンションネーム/タグを新規セット
              const newAttention = this.createAttentionValue(commonAttention, color3.colorCode);
              color3.attentionList.push(newAttention);
            }
          });
        }
      });

    return formattedResults;
  }

  /**
   * アテンション値の設定
   * @param val 00共通のデータ
   * @param colorCode 色コード
   * @returns アテンションデータ
   */
  private createAttentionValue(val: FukukitaruOrderSku, colorCode: string): FukukitaruOrderSku {
    return {
      /** フクキタル発注SKU情報ID. */
      id: null,
      /** フクキタル発注ID. */
      fOrderId: val.fOrderId,
      /** カラーコード. */
      colorCode: colorCode,
      /** サイズ. */
      size: null,  // アテンションネームにサイズは無いためnull
      /** 資材ID. */
      materialId: val.materialId,
      /** 資材数量. */
      orderLot: 0,
      /** 資材種類. */
      materialType: val.materialType,
      /** 資材種類名. */
      materialTypeName: val.materialTypeName,
      /** 資材コード. */
      materialCode: val.materialCode,
      /** 資材コード名. */
      materialCodeName: val.materialCodeName,
      /** 並び順. */
      sortOrder: val.sortOrder,
      /** 出荷単位.(画面表示用) */
      moq: null  // アテンションネームに出荷単位は無いためnull
    };
  }

  /**
   * メインの画面への返却前に選択行のデータの並び順を振りなおす処理
   * @returns 並び順でソートした結果
   */
  private emptySortOrderItems(): FukukitaruAttentionByColor[] {

    /** 選択行全件を保持する配列 */
    const mapList: FukukitaruAttentionByColor[] = [];
    /** 並び順(sortOrder)入力ありのみのデータを保持する配列 */
    const sortOrderList: FukukitaruAttentionByColor[] = [];
    /** 並び順(sortOrder)入力なしのみのデータを保持する配列 */
    const notSortOrderList: FukukitaruAttentionByColor[] = [];
    const result: FukukitaruAttentionByColor[] = [];

    // 1. sortOrderが空文字のため数値に変換する
    this.selectedItems.map(item => {
      mapList.push({
        colorCode: item.colorCode,
        colorName: item.colorName,
        attentionList: item.attentionList.map(val => {
          return {
            id: val.id,
            fOrderId: val.fOrderId,
            colorCode: val.colorCode,
            size: val.size,
            materialId: val.materialId,
            orderLot: val.orderLot,
            materialType: val.materialType,
            materialTypeName: val.materialTypeName,
            materialCode: val.materialCode,
            materialCodeName: val.materialCodeName,
            sortOrder: Number(val.sortOrder),
            moq: val.moq
          };
        }) as FukukitaruOrderSku[]
      });
    });

    // 2. sortOrder入力ありの配列生成
    mapList.map(item => {
      sortOrderList.push({
        colorCode: item.colorCode,
        colorName: item.colorName,
        attentionList: item.attentionList.filter(val => val.sortOrder !== 0)
      });
    });

    // 3. sortOrder入力なしの配列生成
    mapList.map(item => {
      notSortOrderList.push({
        colorCode: item.colorCode,
        colorName: item.colorName,
        attentionList: item.attentionList.filter(val => val.sortOrder === 0)
      });
    });

    // 4. sortOrder入力ありのデータをソートする
    sortOrderList.forEach(val => {
      val.attentionList.sort((a, b) => {
        if (a.sortOrder === b.sortOrder) {
          // 同順の場合はコード順
          return (a.materialCode as any > b.materialCode as any) ? 1 : -1;
        } else {
          // 並び順
          return a.sortOrder - b.sortOrder;
        }
      });
    });

    // 5. sortOrder入力なしのデータをソートする
    notSortOrderList.forEach(val => {
      val.attentionList.sort((a, b) => {
        // 同順の場合はコード順
        return (a.materialCode as any > b.materialCode as any) ? 1 : -1;
      });
    });

    // 6. sortOrder入力ありのデータとsortOrder入力なしのデータを合わせる
    sortOrderList.map(item => {
      const notSortOrderData = notSortOrderList
        .filter(val => val.colorCode === item.colorCode);

      result.push({
        colorCode: item.colorCode,
        colorName: item.colorName,
        attentionList: item.attentionList.concat(notSortOrderData[0].attentionList)
      });

      // sortOrderを1から振りなおす
      result.forEach(byColorItem => {
        let count = 1;
        byColorItem.attentionList.map(val => {
          val.sortOrder = count;
          count++;
        });
      });
    });

    return result;
  }

  /**
   * 列選択時の処理.
   * @param item
   */
  onSelectRow(selectedItem: ResultItem): void {
    this.overallErrorMsgCode = '';

    // 行を選択状態を切り替える
    selectedItem.selected = !selectedItem.selected;

    // 選択可能数以下ならtrue
    const selectFlg = this.resultItems.filter(value1 => value1.selected === true).length <= this.MAX_SELECT_COUNT;

    if (selectFlg) {
      // 並び順に以下の値を設定
      // - 行が選択された場合、選択可能な個数+1
      // - 行の選択が外された場合、null
      selectedItem.item.sortOrder = selectedItem.selected ? this.MAX_SELECT_COUNT + 1 : null;

      // 並び順でソートした結果を選択結果に設定
      this.selectedItems.filter(item => item.colorCode === this.selectedColorCode)[0].attentionList = this.sortOderItems();
    } else {
      // 行を選択状態を切り替える(元に戻す)
      selectedItem.selected = !selectedItem.selected;
      // 選択数が限界数を超える場合はエラーメッセージ表示
      this.overallErrorMsgCode = 'ERRORS.SELECT_ITEM_OVER';
    }
  }

  /**
   * チェックボックス押下時、選択可能数を超えている場合はチェックを外して、エラーメッセージを表示します。
   * @param selectedItem 選択された行のデータ
   */
  onSelectCheckbox(event: any): void {
    this.overallErrorMsgCode = '';
    let selectFlg = false;

    // チェックボックスクリック時はvalue1.selectedがtrueの行数は3以上になることは無いため
    selectFlg = this.resultItems.filter(value1 => value1.selected === true).length < this.MAX_SELECT_COUNT;  // 選択可能数以下ならtrue

    if (!selectFlg) {
      event.checked = false;
      this.overallErrorMsgCode = 'ERRORS.SELECT_ITEM_OVER';  // 選択数が3個を超える場合はエラーメッセージ表示
    }
  }

  /**
   * 並び順のフォーカスアウト時の処理.
   * @param selectedItem 選択行
   */
  onBlurSortOrder(selectedItem: ResultItem): void {
    if (!(/^[1-9,]*$/).test(String(selectedItem.item.sortOrder))) {
      // 1～9以外が入力された場合、並び順を空にする
      selectedItem.item.sortOrder = null;
    }
  }

  /**
   * 並び順を振り直し、並び順でソートした結果を取得する.
   * 並び順が同じ場合は、資材コードの昇順とする。
   * @returns 並び順でソートした結果
   */
  private sortOderItems(): FukukitaruOrderSku[] {
    let sortOrder = 1;

    return this.resultItems
      .filter(item => item.selected)
      .sort((a, b) => {
        if (a.item.sortOrder === b.item.sortOrder) {
          return (a.master.materialCode as any > b.master.materialCode as any) ? 1 : -1;
        } else {
          return a.item.sortOrder - b.item.sortOrder;
        }
      })
      .map(item => {
        item.item.sortOrder = sortOrder++;
        return item.item;
    });
  }

  /**
   * ファイルダウンロードリンク押下処理.
   * @param fukukitaruMasterType 資材種別
   */
  onFileDownLoad(fukukitaruMasterType: FukukitaruMasterType): void {
    this.overallErrorMsgCode = '';
    this.materialFileList.some(materialFile => {
      if (materialFile.masterType === fukukitaruMasterType) {
        this.fileService.fileDownload(materialFile.fileNoId.toString()).subscribe(res => {
          const data = this.fileService.splitBlobAndFileName(res);
          FileUtils.downloadFile(data.blob, data.fileName);
        }, () => this.overallErrorMsgCode = 'ERRORS.FILE_DL_ERROR');
        return true;
      }
    });
  }

}
