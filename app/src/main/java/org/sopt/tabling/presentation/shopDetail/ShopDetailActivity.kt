package org.sopt.tabling.presentation.shopDetail

import android.animation.ArgbEvaluator
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Bundle
import android.view.ContextThemeWrapper
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.chip.Chip
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.sopt.tabling.R
import org.sopt.tabling.databinding.ActivityShopDetailBinding
import org.sopt.tabling.domain.model.ShopDetail
import org.sopt.tabling.presentation.common.ViewModelFactory
import org.sopt.tabling.presentation.visitPerson.VisitPersonBottomSheetDialogFragment
import org.sopt.tabling.util.UiState
import org.sopt.tabling.util.binding.BindingActivity
import org.sopt.tabling.util.extension.setRatingBar

class ShopDetailActivity :
    BindingActivity<ActivityShopDetailBinding>(R.layout.activity_shop_detail) {
    private val shopDetailViewModel: ShopDetailViewModel by viewModels { ViewModelFactory() }
    private lateinit var shopDetailShopImgAdapter: ShopDetailShopImgAdapter
    private lateinit var shopDetailMenuListAdapter: ShopDetailMenuListAdapter
    private lateinit var shopDetailRecentReviewAdapter: ShopDetailRecentReviewAdapter
    private val convertDetailStarValue: (Float) -> Int = { value -> (value * 20).toInt() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.shopDetailViewModel = shopDetailViewModel

        initLayout()
        addListeners()
        collectData()
    }

    private fun initLayout() {
        initAppBar()
        initTabLayout()
        initNestedScrollView()
    }

    private fun addListeners() {
        binding.btnShopDetailReserve.setOnClickListener {
            VisitPersonBottomSheetDialogFragment().show(supportFragmentManager, VISIT_PERSON)
        }
    }

    private fun collectData() {
        shopDetailViewModel.getShopDetailState.flowWithLifecycle(lifecycle).onEach { uiState ->
            when (uiState) {
                is UiState.Success -> {
                    uiState.data?.let { shopDetail ->
                        setShopImgViewPager(shopDetail)
                        setShopDetailAppBar(shopDetail)
                        setShopDetailHome(shopDetail)
                        setShopDetailMenuList(shopDetail)
                        setShopDetailRecentReview(shopDetail)
                    }
                }

                else -> Unit
            }

        }.launchIn(lifecycleScope)
    }

    private fun initAppBar() {
        with(binding) {
            ablShopDetail.addOnOffsetChangedListener { appBarLayout, verticalOffset ->
                val maxScroll = appBarLayout.totalScrollRange
                val percentage = Math.abs(verticalOffset).toFloat() / maxScroll.toFloat()

                tbShopDetail.setBackgroundColor(
                    ArgbEvaluator().evaluate(
                        percentage,
                        Color.TRANSPARENT,
                        Color.WHITE
                    ) as Int
                )

                val iconEvaluator = ArgbEvaluator().evaluate(
                    percentage,
                    ContextCompat.getColor(this@ShopDetailActivity, R.color.white),
                    ContextCompat.getColor(this@ShopDetailActivity, R.color.gray_800)
                ) as Int

                ivShopDetailBack.setImageDrawable(
                    ContextCompat.getDrawable(
                        this@ShopDetailActivity,
                        R.drawable.ic_back_white_45
                    )?.mutate()?.apply {
                        colorFilter = PorterDuffColorFilter(iconEvaluator, PorterDuff.Mode.SRC_IN)
                    }
                )

                ivShopDetailHeart.setImageDrawable(
                    ContextCompat.getDrawable(
                        this@ShopDetailActivity,
                        R.drawable.ic_heart_white_24
                    )?.mutate()?.apply {
                        colorFilter = PorterDuffColorFilter(iconEvaluator, PorterDuff.Mode.SRC_IN)
                    }
                )

                ivShopDetailShare.setImageDrawable(
                    ContextCompat.getDrawable(
                        this@ShopDetailActivity,
                        R.drawable.ic_share_white_24
                    )?.mutate()?.apply {
                        colorFilter = PorterDuffColorFilter(iconEvaluator, PorterDuff.Mode.SRC_IN)
                    }
                )
            }
        }
    }

    private fun initTabLayout() {
        binding.tlShopDetail.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    HOME -> binding.nsvShopDetailContent.scrollTo(
                        Y_VALUE,
                        binding.layoutShopDetailHome.top
                    )

                    MENU_LIST -> binding.nsvShopDetailContent.scrollTo(
                        Y_VALUE,
                        binding.layoutShopDetailMenuList.top
                    )

                    RECENT_REVIEW -> binding.nsvShopDetailContent.scrollTo(
                        Y_VALUE,
                        binding.layoutShopDetailRecentReview.top
                    )
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) = Unit

            override fun onTabReselected(tab: TabLayout.Tab?) = Unit
        }
        )
    }

    private fun initNestedScrollView() {
        binding.nsvShopDetailContent.setOnScrollChangeListener(
            NestedScrollView.OnScrollChangeListener { _, _, scrollY, _, _ ->
                when (scrollY) {
                    in binding.layoutShopDetailHome.top until binding.layoutShopDetailMenuList.top -> binding.tlShopDetail.setScrollPosition(
                        HOME,
                        FIRST_POSITION.toFloat(),
                        true
                    )

                    in binding.layoutShopDetailMenuList.top until binding.layoutShopDetailRecentReview.top -> binding.tlShopDetail.setScrollPosition(
                        MENU_LIST,
                        FIRST_POSITION.toFloat(),
                        true
                    )

                    in binding.layoutShopDetailRecentReview.top until binding.layoutShopDetailRecentReview.bottom -> binding.tlShopDetail.setScrollPosition(
                        RECENT_REVIEW,
                        FIRST_POSITION.toFloat(),
                        true
                    )
                }
            }
        )
    }

    private fun setShopImgViewPager(shopDetail: ShopDetail) {
        shopDetailShopImgAdapter = ShopDetailShopImgAdapter()
        binding.vpShopDetailShopImg.adapter = shopDetailShopImgAdapter
        shopDetailShopImgAdapter.submitList(shopDetail.detailPhotoList)
        setTvShopDetailShopImgPageText(FIRST_POSITION)

        binding.vpShopDetailShopImg.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                setTvShopDetailShopImgPageText(position)
            }
        }
        )
    }

    private fun setShopDetailAppBar(shopDetail: ShopDetail) {
        with(binding) {
            chipShopDetailWaiting.text = getString(
                R.string.shop_detail_waiting,
                shopDetail.currentWaiting
            )
            tvShopDetailShopInfoName.text = shopDetail.name
            tvShopDetailShopInfoAddress.text = shopDetail.longAddress
            tvShopDetailInfoStar.text = shopDetail.averageStar.toString()
            includeShopDetailInfoStar.setRatingBar(shopDetail.averageStar)
            tvShopDetailInfoReviewCount.text = getString(
                R.string.shop_detail_review_count,
                shopDetail.reviewCount
            )
        }
    }

    private fun setShopDetailHome(shopDetail: ShopDetail) {
        with(binding) {
            tvShopDetailHomeSalesTime.text = shopDetail.salesTime
            tvShopDetailHomeReserve.text = shopDetail.waitingTime
            tvShopDetailHomeRestTime.text = shopDetail.restTime
            tvShopDetailHomeRestDay.text = shopDetail.restDay
            tvShopDetailHomePhoneNumber.text = shopDetail.phoneNumber

            shopDetail.hashTagList.forEachIndexed { _, hashTag ->
                cgShopDetailHomeShopPick.addView(
                    Chip(
                        ContextThemeWrapper(
                            this@ShopDetailActivity,
                            R.style.Widget_Material3_Chip_Style_Radius99_Pick
                        )
                    ).apply {
                        text = hashTag
                        setTextAppearance(R.style.TextAppearance_Caption1_SemiBold)
                        setTextColor(
                            ContextCompat.getColor(
                                this@ShopDetailActivity,
                                R.color.gray_400
                            )
                        )
                        setEnsureMinTouchTargetSize(false)
                        chipBackgroundColor = ColorStateList.valueOf(
                            ContextCompat.getColor(
                                this@ShopDetailActivity,
                                R.color.white
                            )
                        )
                    }
                )
            }

            tvShopDetailHomeIntroduceContent.text = shopDetail.introduceContent
        }
    }

    private fun setShopDetailMenuList(shopDetail: ShopDetail) {
        with(binding) {
            shopDetailMenuListAdapter = ShopDetailMenuListAdapter()
            rvShopDetailMenuList.adapter = shopDetailMenuListAdapter
            shopDetailMenuListAdapter.submitList(shopDetail.menuList)
            includeShopDetailMenuListDetailBtn.tvDetailBtn.text =
                getString(R.string.shop_detail_menu_list_full_menu_detail)
        }
    }

    private fun setShopDetailRecentReview(shopDetail: ShopDetail) {
        with(binding) {
            tvShopDetailRecentReviewTitle.text = getString(
                R.string.shop_detail_recent_review_title,
                shopDetail.reviewCount
            )
            tvShopDetailRecentReviewTotalStarContext.text = shopDetail.averageStar.toString()
            includeShopDetailRecentReviewTotalStar.setRatingBar(shopDetail.averageStar)
            includeShopDetailRecentReviewDetailStarFoodTaste.tvProgressBarTitle.text =
                getString(R.string.shop_detail_recent_review_food_taste)
            includeShopDetailRecentReviewDetailStarFoodTaste.pbProgressBar.progress =
                convertDetailStarValue(shopDetail.detailStarList[FOOD_TASTE])
            includeShopDetailRecentReviewDetailStarFoodTaste.tvProgressBarScore.text =
                getString(
                    R.string.shop_detail_recent_review_score,
                    shopDetail.detailStarList[FOOD_TASTE]
                )
            includeShopDetailRecentReviewDetailStarMood.tvProgressBarTitle.text =
                getString(R.string.shop_detail_recent_review_mood)
            includeShopDetailRecentReviewDetailStarMood.pbProgressBar.progress =
                convertDetailStarValue(shopDetail.detailStarList[MOOD])
            includeShopDetailRecentReviewDetailStarMood.tvProgressBarScore.text =
                getString(
                    R.string.shop_detail_recent_review_score,
                    shopDetail.detailStarList[MOOD]
                )
            includeShopDetailRecentReviewDetailStarKindness.tvProgressBarTitle.text =
                getString(R.string.shop_detail_recent_review_kindness)
            includeShopDetailRecentReviewDetailStarKindness.pbProgressBar.progress =
                convertDetailStarValue(shopDetail.detailStarList[KINDNESS])
            includeShopDetailRecentReviewDetailStarKindness.tvProgressBarScore.text =
                getString(
                    R.string.shop_detail_recent_review_score,
                    shopDetail.detailStarList[KINDNESS]
                )
            includeShopDetailRecentReviewDetailStarCleanliness.tvProgressBarTitle.text =
                getString(R.string.shop_detail_recent_review_cleanliness)
            includeShopDetailRecentReviewDetailStarCleanliness.pbProgressBar.progress =
                convertDetailStarValue(shopDetail.detailStarList[CLEANLINESS])
            includeShopDetailRecentReviewDetailStarCleanliness.tvProgressBarScore.text =
                getString(
                    R.string.shop_detail_recent_review_score,
                    shopDetail.detailStarList[CLEANLINESS]
                )
            shopDetailRecentReviewAdapter = ShopDetailRecentReviewAdapter()
            rvShopDetailRecentReview.adapter = shopDetailRecentReviewAdapter
            shopDetailRecentReviewAdapter.submitList(shopDetail.reviewList)
            includeShopDetailRecentReviewDetailBtn.tvDetailBtn.text =
                getString(R.string.shop_detail_recent_review_full_review_detail)
        }
    }

    private fun setTvShopDetailShopImgPageText(currentPage: Int) {
        binding.tvShopDetailShopImgPage.text = getString(
            R.string.shop_detail_shop_img_page,
            currentPage + 1,
            shopDetailShopImgAdapter.currentList.size
        )
    }

    companion object {
        const val FIRST_POSITION = 0
        const val Y_VALUE = 0
        const val FOOD_TASTE = 0
        const val MOOD = 1
        const val KINDNESS = 2
        const val CLEANLINESS = 3
        const val HOME = 0
        const val MENU_LIST = 1
        const val RECENT_REVIEW = 2
        const val VISIT_PERSON = "visitPersonBottomSheetDialogFragment"
    }
}
