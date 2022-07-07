package com.android.aschat.feature_home.presentation.mine

import android.Manifest
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import coil.load
import com.android.aschat.R
import com.android.aschat.databinding.HomeUserEditFragmentBinding
import com.android.aschat.feature_home.domain.model.mine.EditDetail
import com.android.aschat.feature_home.presentation.HomeEvents
import com.android.aschat.feature_home.presentation.HomeViewModel
import com.android.aschat.util.*
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog

class UserEditFragment: Fragment() {

    private lateinit var mBinding: HomeUserEditFragmentBinding
    private val mViewModel: HomeViewModel by activityViewModels()
    // 存储当前选择的头像的路径
    private var mAvatarSrcPath: String = ""

    private val mLoadingDialog: Dialog by lazy {
        DialogUtil.createLoadingDialog(requireContext())
    }

    /**
     * 进入相册选择照片
     */
    private val mGetImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri == null) return@registerForActivityResult
        // 把照片更新
        mBinding.editHead.load(uri)
        // 更新字段
        val originalPath = UriUtils.getFileAbsolutePath(requireContext(), uri)!!
        ImageUtil.compress(originalPath)?.let {
            mAvatarSrcPath = it
        }
    }

    /**
     * Android11 请求存储权限
     */
    private val mGetStoragePermissionOver11 = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (MobileButlerUtil.checkExternalStorageInAllAndroid(requireContext())) {
            // 有权限，去相册选择照片
            getImageFromGallery()
        }else {
            // 算了，什么也不做

        }
    }

    /**
     * Android 11以下，请求权限，正常请求
     */
    private val mGetStoragePermissionLess11 = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
        if (it) {
            // 有权限，去相册选择照片
            getImageFromGallery()
        }else {
            if(!ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                // 当点击了不再询问之后，shouldShowRequestPermissionRationale返回false
                val intent = MobileButlerUtil.getDefaultSettingIntent(requireContext())
                mGetStoragePermissionLess11Rational.launch(intent)
            }else {
                // 算了

            }
        }
    }

    /**
     * Android 11以下，请求权限，点了不正常询问之后的请求
     */
    private val mGetStoragePermissionLess11Rational = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (MobileButlerUtil.checkExternalStorageInAllAndroid(requireContext())) {
            // 有权限，去相册选择照片
            getImageFromGallery()
        }else {
            // 算了，什么也不做

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = HomeUserEditFragmentBinding.inflate(inflater)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setTypeface()
        initWidget()
    }

    private fun setTypeface() {
        mBinding.editSubmit.typeface = FontUtil.getTypeface(requireContext())
    }

    private fun initWidget() {
        mBinding.apply {
            // 要更改生日
            userEditBirthday.setOnClickListener {
                PickerUtil.showTimePicker(parentFragmentManager) { datePickerDialog: DatePickerDialog, y: Int, m: Int, d: Int ->
                    val year = y.toString()
                    var month = ""
                    var day = ""
                    if (m+1 < 10) month = "0${m+1}" else month = "${m+1}"
                    if (d < 10) day = "0${d}" else day = "$d"
                    this.userEditBirthday.setText("$year-$month-$day")
                }
            }
            // 要更改国家
            userEditCountry.setOnClickListener {
                PickerUtil.showCountryPicker(context = requireContext(), fm = parentFragmentManager) { name,  code,  dialCode,  flagDrawableResID ->
                    this.userEditCountry.setText(name)
                }
            }
            // 打开相册
            editHead.setOnClickListener {
                getImageFromGallery()
            }
            // 监听字数改变
            editAbout.addTextChangedListener(object: TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                var mNumberCount = 0
                val mNumberCountUp = 300

                override fun afterTextChanged(editable: Editable?) {
                    mNumberCount = editable!!.length // 总长度

                    if (mNumberCount > mNumberCountUp) {
                        editable.delete(mNumberCountUp, mNumberCount)// 删除多余的字符
                        mNumberCount = mNumberCountUp
                    }

                    mBinding.editAboutCount.text = "$mNumberCount/$mNumberCountUp"
                }
            })
            // 点击提交
            editSubmit.setOnClickListener {
                mViewModel.onEvent(
                    HomeEvents.SubmitEdit(
                        editDetail = EditDetail(
                            nickName = mBinding.userEditName.text.toString(),
                            birthday = mBinding.userEditBirthday.text.toString(),
                            country = mBinding.userEditCountry.text.toString(),
                            inviteCode = "",
                            about = mBinding.editAbout.text.toString(),
                            avatarSrcPath = mAvatarSrcPath
                        ),
                        onStartSubmit = {
                            mLoadingDialog.show()
                        },
                        onSuccess = {
                            Toast.makeText(requireContext(), getString(R.string.Save_information_successfully), Toast.LENGTH_SHORT).show()
                            mLoadingDialog.dismiss()
                            findNavController().popBackStack()
                        },
                        onFail = {
                            Toast.makeText(requireContext(), getString(R.string.Failed_to_save_information), Toast.LENGTH_SHORT).show()
                            mLoadingDialog.dismiss()
                            findNavController().popBackStack()
                        }
                    ))
            }
            // 点击退出按钮
            userEditBack.setOnClickListener {
                mViewModel.onEvent(HomeEvents.ExitUserEditFragment(findNavController()))
            }
        }

        mViewModel.apply {
            // 初次进入时加载名字，性别，当前头像，个性签名，生日，国家等
            userInfoMoreDetailed.observe(viewLifecycleOwner) {
                mBinding.userEditName.setText(it.nickname)
                mBinding.editHead.load(it.avatarUrl)
                mBinding.editAbout.setText(it.about)
                mBinding.userEditBirthday.setText(it.birthday)
                mBinding.userEditCountry.setText(it.country)
            }
        }
    }

    /**
     * 前往相册选择照片
     */
    private fun getImageFromGallery() {
        if (MobileButlerUtil.checkExternalStorageInAllAndroid(requireContext())) {
            // 去选择照片
            mGetImageLauncher.launch("image/*")
        }else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // > Android 11
            mGetStoragePermissionOver11.launch(MobileButlerUtil.getAndroid11ExternalIntent(requireContext()))
        }else {
            // < Android 11
            mGetStoragePermissionLess11.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
    }
}
