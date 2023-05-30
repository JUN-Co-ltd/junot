import { Injectable } from '@angular/core';
import { CodeMaster } from 'src/app/const/code-master';
import { NumberUtilsService } from './number-utils.service';

@Injectable({
  providedIn: 'root'
})
export class SeasonService {

  constructor(
    private numberUtils: NumberUtilsService
  ) { }

  /**
   * サブシーズンコード名を取得する.
   * @param subSeasonId サブシーズンID
   * @returns サブシーズンコード名
   */
  findSubSeasonValue(subSeasonId: string | number): string | null {
    const id = this.numberUtils.defaultNull(subSeasonId);
    const target = CodeMaster.subSeason.find(codeItem => codeItem.id === id);
    return target == null ? null : target.value;
  }

  /**
   * @returns シーズンリストフォーム値
   */
  generateSeasonsFormValues(): { id: number, value: string, selected: boolean }[] {
    return CodeMaster.subSeason
      .map(subSeason => ({ id: subSeason.id, value: subSeason.value, selected: false }));
  }
}
