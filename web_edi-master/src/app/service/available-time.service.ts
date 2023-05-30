import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { HttpClient } from '@angular/common/http';

import { environment } from '../../environments/environment';

import { AvailableTime } from '../model/available-time';

const BASE_URL = `${ environment.apiBaseUrl }/availableTimes`;

@Injectable({
  providedIn: 'root'
})
export class AvailableTimeService {
  constructor(
    private http: HttpClient
  ) { }

  /**
   * JUNoT利用時間を取得する.
   */
  public get(): Observable<AvailableTime> {
    return this.http.get<AvailableTime>(
      BASE_URL
    );
  }
}
