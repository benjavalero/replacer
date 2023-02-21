import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { Observable } from 'rxjs';
import { PublicIp } from '../../api/models/public-ip';
import { AdministrationService } from '../../api/services/administration.service';

@Component({
  standalone: true,
  selector: 'app-public-ip',
  imports: [CommonModule],
  templateUrl: './public-ip.component.html',
  styleUrls: []
})
export class PublicIpComponent implements OnInit {
  publicIp$!: Observable<PublicIp>;

  constructor(private administrationService: AdministrationService) {}

  ngOnInit(): void {
    this.publicIp$ = this.administrationService.getPublicIp();
  }
}
