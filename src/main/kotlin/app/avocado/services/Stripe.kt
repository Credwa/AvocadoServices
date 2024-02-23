package app.avocado.services

import app.avocado.SupabaseConfig.supabaseAdmin
import app.avocado.models.StripeAccount
import com.stripe.StripeClient
import com.stripe.model.Account
import com.stripe.model.AccountLink
import com.stripe.model.Balance
import com.stripe.net.RequestOptions
import com.stripe.param.AccountCreateParams
import com.stripe.param.AccountLinkCreateParams
import com.stripe.param.BalanceRetrieveParams
import io.github.jan.supabase.postgrest.from


class Stripe(private val userId: String) {
    private var accountId: String? = null
    suspend fun createStripeAccount() {
        val params =
            AccountCreateParams.builder().setType(AccountCreateParams.Type.EXPRESS).build()
        val account: Account = Account.create(params)
        supabaseAdmin.from("stripe_accounts").insert(StripeAccount(userId, account.id))
        accountId = account.id
    }

    fun createAccountLink(returnUrl: String, refreshUrl: String): String {
        val params =
            AccountLinkCreateParams.builder()
                .setAccount(accountId)
                .setRefreshUrl(returnUrl)
                .setReturnUrl(refreshUrl)
                .setType(AccountLinkCreateParams.Type.ACCOUNT_ONBOARDING)
                .build()

        val accountLink = AccountLink.create(params)

        return accountLink.url
    }

    fun getAccountInfo(): Account {
        val accountInfo = Account.retrieve(accountId)
        return accountInfo
    }

    fun getAccountBalance(): Balance {
        val params = BalanceRetrieveParams.builder().build()
        val requestOptions =
            RequestOptions.builder().setStripeAccount(accountId).build()
        val client =
            StripeClient(System.getenv("STRIPE_API_KEY"))
        val balance: Balance = client.balance().retrieve(params, requestOptions)

        return balance
    }

    fun setAccountId(accId: String) {
        accountId = accId
    }
}