package com.lemontree.domain.wallet.exception

import com.lemontree.global.exception.*

class BalanceMaxedOutException(message: String) : BusinessException(message) {
}