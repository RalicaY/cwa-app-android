package de.rki.coronawarnapp.contactdiary.ui.onboarding

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.ActionOnlyNavDirections
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.contactdiary.ui.ContactDiaryActivity
import de.rki.coronawarnapp.databinding.ContactDiaryOnboardingFragmentBinding
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.doNavigate
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.viewBindingLazy
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import javax.inject.Inject

class ContactDiaryOnboardingFragment : Fragment(R.layout.contact_diary_onboarding_fragment), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val vm: ContactDiaryOnboardingFragmentViewModel by cwaViewModels { viewModelFactory }
    private val binding: ContactDiaryOnboardingFragmentBinding by viewBindingLazy()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            contactDiaryOnboardingNextButton.setOnClickListener {
                vm.onNextButtonClick()
            }

            contactDiaryOnboardingButtonBack.headerButtonBack.buttonIcon.setOnClickListener {
                vm.onBackButtonPress()
            }

            contactDiaryOnboardingPrivacyInformation.setOnClickListener {
                vm.onPrivacyButtonPress()
            }
        }

        vm.routeToScreen.observe2(this) {
            when (it) {

                ContactDiaryOnboardingNavigationEvents.NavigateToMainActivity -> {
                    (requireActivity() as ContactDiaryActivity).finish()
                }

                ContactDiaryOnboardingNavigationEvents.NavigateToPrivacyFragment -> {
                    doNavigate(
                        ActionOnlyNavDirections(
                            R.id.action_contactDiaryOnboardingFragment_to_contactDiaryInformationPrivacyFragment
                        )
                    )
                }
            }
        }
    }
}