package chain.fxgj.core.common.service;

import chain.fxgj.server.payroll.web.UserPrincipal;

public interface PushSyncDataService {

    void pushSyncDataToCache(String idNumber, String groupId, String year, String type, UserPrincipal principal);
}
