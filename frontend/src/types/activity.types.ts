export interface ActivityLogItem {
  id: string;
  activityType: string;
  entityType: 'FILE' | 'FOLDER';
  entityId: string;
  entityName: string;
  details: string;
  createdAt: string;
}
