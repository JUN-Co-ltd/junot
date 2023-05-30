import { Component, OnInit, Input } from '@angular/core';
import { FormGroup, FormControl } from '@angular/forms';
import { Location } from '@angular/common';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

import { ScreenSettingFukukiatru } from '../../model/screen-setting-fukukitaru';
import { TabModel } from '../../model/tab-model';
import { FukukitaruMasterAppendicesTerm } from '../../model/fukukitaru-master-appendices-term';
import { FukukitaruAppendicesTermByColor } from '../../interface/fukukitaru-appendices-term-by-color';
import { FukukitaruAppendicesTerm } from '../../interface/fukukitaru-appendices-term';

import { FukukitaruOrder01Service } from '../../service/fukukitaru-order01.service';
import { SwitchTabService } from '../../service/switch-tab.service';
import { FileService } from '../../service/file.service';

import { FukukitaruMasterType } from '../../const/const';

import { FileUtils } from '../../util/file-utils';

class ResultItem {
  item: FukukitaruAppendicesTerm;
  selected: boolean;
}

@Component({
  selector: 'app-appendices-term-modal',
  templateUrl: './appendices-term-modal.component.html',
  styleUrls: ['./appendices-term-modal.component.scss']
})
export class AppendicesTermModalComponent implements OnInit {
  /** htmlから参照したい定数を定義 */
  readonly FUKUKITARU_MASTER_TYPE = FukukitaruMasterType;
  /** 選択可能な個数 */
  readonly MAX_SELECT_COUNT: number = 12;
  readonly EXCEEDED_MAX_SELECT_COUNT: number = 13;

  @Input() listMasterType = null;  // マスタタイプリスト
  @Input() fukukitaruMaster: ScreenSettingFukukiatru = null;  // フクキタルマスタデータ
  @Input() appendicesTermByColorList: FukukitaruAppendicesTermByColor[] = null;


  searchFormGroup: FormGroup;  // 検索フォームグループ
  searchLoading: boolean;      // 検索中フラグ（連続クリック防止用）
  resultItems: ResultItem[] = [];  // 検索結果
  selectedItems: FukukitaruAppendicesTermByColor[] = [];  // 選択中の行
  nextPageToken: string;  // 次のページのトークン
  appendicesTermMaster: FukukitaruMasterAppendicesTerm[] = []; // 付記用語マスタデータ

  washAppendicesTerm: boolean;      // タイトル(洗濯ネーム付記用語)
  attentionAppendicesTerm: boolean; // タイトル(アテンションタグ付記用語)

  currentTab: any; // 表示するタブ
  tabs: TabModel[] = new Array<TabModel>(); // タブ
  selectedColorCode: string;  // 表示するカラーのコード保持用

  overallErrorMsgCode = '';  // 画面全体にかかる異常系メッセージ

  constructor(
    public activeModal: NgbActiveModal,
    private location: Location,
    private switchTabService: SwitchTabService,
    public fukukitaruOrder01Service: FukukitaruOrder01Service,
    private fileService: FileService
  ) { }

  ngOnInit() {
    // 検索パラメタをフォームに設定
    this.searchFormGroup = new FormGroup({
      listMasterType: new FormControl(this.listMasterType),
    });

    // マスタタイプによって設定を分ける
    switch (this.listMasterType) {
      case FukukitaruMasterType.WASH_NAME_APPENDICES_TERM: // 洗濯ネーム付記用語
        this.washAppendicesTerm = true;  // タイトル
        this.appendicesTermMaster = this.fukukitaruMaster.listWashNameAppendicesTerm;
        break;
      case FukukitaruMasterType.ATTENTION_TAG_APPENDICES_TERM: // アテンションタグ付記用語
        this.attentionAppendicesTerm = true;  // タイトル
        this.appendicesTermMaster = this.fukukitaruMaster.listAttentionTagAppendicesTerm;
        break;
      default:
        break;
    }

    this.setInitTab(); // タブ表示

    this.display(this.appendicesTermMaster);  // データ表示
  }

  /**
   *  タブの初期表示処理を行う。
   */
  private setInitTab(): void {
    // タブを設定する
    this.appendicesTermByColorList.forEach(appendicesTermByColor => {
      this.tabs.push(new TabModel(appendicesTermByColor.colorCode + ' ' + appendicesTermByColor.colorName, null, false));
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
   * @param defaultCompanyId 親ページで設定済の会社id
   */
  private display(
    appendicesTermMaster: FukukitaruMasterAppendicesTerm[]): void {
    // 全カラーのデータを保持
    this.selectedItems = JSON.parse(JSON.stringify(this.appendicesTermByColorList));

    // 選択したカラーのみデータを抽出
    const selectedAppendicesTermByColor = this.appendicesTermByColorList.filter(
      appendicesTerm => appendicesTerm.colorCode === this.selectedColorCode);

    // 選択したカラーのみ画面表示用に変換
    this.resultItems = appendicesTermMaster.map(mstData => this.toAppendicesTerm(mstData, selectedAppendicesTermByColor[0]));
  }

  /**
   * マスタデータを画面表示用に変換します。
   * @param mstData マスタデータ
   * @return 検索結果に表示する結果
   */
  private toAppendicesTerm(fukukitaruMaster: FukukitaruMasterAppendicesTerm,
    selectedAppendicesTermByColor: FukukitaruAppendicesTermByColor): ResultItem {
    const selected = selectedAppendicesTermByColor.appendicesTermList
        .some(selectedAppendicesTerm => selectedAppendicesTerm.appendicesTermId === fukukitaruMaster.id);

    const item = {
      id: null,
      appendicesTermId: fukukitaruMaster.id,  // 付記用語マスタID
      appendicesTermCode: fukukitaruMaster.appendicesTermCode,  // 付記用語コード
      appendicesTermCodeName: fukukitaruMaster.appendicesTermCodeName,  // 付記用語コード名
      appendicesTermSentence: fukukitaruMaster.appendicesTermSentence,  // 付記用語文章
    } as FukukitaruAppendicesTerm;
    return {
      item: item,
      selected: selected
    } as ResultItem;
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
    const selectedAppendicesTermByColor = this.selectedItems
      .filter(appendicesTerm => appendicesTerm.colorCode === this.selectedColorCode);

    // 選択したカラーのみ画面表示用に変換
    this.resultItems = this.appendicesTermMaster.map(mstData => this.toAppendicesTerm(mstData, selectedAppendicesTermByColor[0]));
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
   * 選択ボタンクリック時、親ページに選択行のデータを渡して、モーダルを閉じます。
   */
  onSelect(): void {
    // コード(付記用語マスタのID順)の昇順
    this.selectedItems.forEach(item => {
      item.appendicesTermList =
        item.appendicesTermList.sort((a, b) => (a.appendicesTermCode as any > b.appendicesTermCode as any) ? 1 : -1);
    });

    this.activeModal.close(this.selectedItems);
  }

  /**
   * 行選択時、選択行のデータを保持し、選択行にのみselectedクラスを付与します。
   * @param selectedItem 選択された行のデータ
   */
  onSelectRow(selectedItem: ResultItem): void {
    this.overallErrorMsgCode = '';
    let selectFlg = false;

    selectedItem.selected = !selectedItem.selected;   // 行を選択状態を切り替える

    selectFlg = this.resultItems.filter(value1 => value1.selected === true).length <= this.MAX_SELECT_COUNT;  // 選択12個以下ならtrue

    if (selectFlg) {
      this.selectedItems.forEach(item => {
        if (item.colorCode === this.selectedColorCode) {
          if (selectedItem.selected) {
            // 選択状態のため追加
            item.appendicesTermList.push(selectedItem.item);
          } else {
            // 未選択状態のため削除
            const spliceId = item.appendicesTermList.findIndex(value => value.appendicesTermId === selectedItem.item.appendicesTermId);
            item.appendicesTermList.splice(spliceId, 1);
          }
        }
      });
    } else {
      selectedItem.selected = !selectedItem.selected;   // 行を選択状態を切り替える(元に戻す)
      this.overallErrorMsgCode = 'ERRORS.SELECT_ITEM_OVER';  // 選択数が12個を超える場合はエラーメッセージ表示
    }
  }

  /**
   * チェックボックス押下時、選択可能数を超えている場合はチェックを外して、エラーメッセージを表示します。
   * @param selectedItem 選択された行のデータ
   */
  onSelectCheckbox(event: any): void {
    this.overallErrorMsgCode = '';
    let selectFlg = false;

    // チェックボックスクリック時はvalue1.selectedがtrueの行数は12以上になることは無いため
    selectFlg = this.resultItems.filter(value1 => value1.selected === true).length < this.MAX_SELECT_COUNT;  // 選択12個未満ならtrue

    if (!selectFlg) {
      event.checked = false;
      this.overallErrorMsgCode = 'ERRORS.SELECT_ITEM_OVER';  // 選択数が12個を超える場合はエラーメッセージ表示
    }
  }

  /**
   * ファイルダウンロードリンク押下処理.
   * @param fukukitaruMasterType 資材種別
   */
  onFileDownLoad(fukukitaruMasterType: FukukitaruMasterType): void {
    this.overallErrorMsgCode = '';
    this.fukukitaruMaster.listMaterialFile.some(materialFile => {
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
