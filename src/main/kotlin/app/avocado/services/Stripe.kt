package app.avocado.services

import app.avocado.SupabaseConfig.supabaseAdmin
import app.avocado.models.StripeAccount
import com.stripe.model.Account
import com.stripe.model.AccountLink
import com.stripe.param.AccountCreateParams
import com.stripe.param.AccountLinkCreateParams
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

    suspend fun createAccountLink(): AccountLink {
        val params =
            AccountLinkCreateParams.builder()
                .setAccount(accountId)
                .setRefreshUrl("https://example.com/reauth")
                .setReturnUrl("https://example.com/return")
                .setType(AccountLinkCreateParams.Type.ACCOUNT_ONBOARDING)
                .build()

        val accountLink = AccountLink.create(params)

        return accountLink
    }
}