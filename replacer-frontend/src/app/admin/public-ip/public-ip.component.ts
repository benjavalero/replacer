import { Component, OnInit } from '@angular/core';
import { Observable } from 'rxjs';
import {AdminService} from "../admin.service";
import { PublicIp } from './public-ip.model';

@Component({
  selector: 'app-public-ip',
  templateUrl: './public-ip.component.html',
  styleUrls: []
})
export class PublicIpComponent implements OnInit {
  publicIp$!: Observable<PublicIp>;

  constructor(private adminService: AdminService) {}

  ngOnInit(): void {
    this.publicIp$ = this.adminService.getPublicIp$();
  }
}
