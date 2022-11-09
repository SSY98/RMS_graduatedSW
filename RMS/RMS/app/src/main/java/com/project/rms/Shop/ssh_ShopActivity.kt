package com.project.rms.Shop

import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.project.rms.App
import com.project.rms.R
import com.project.rms.Recipe.RecipeMake.ssy_RecipeMakeActivity
import com.project.rms.Recipe.ssy_Parts_Ladapter
import com.project.rms.Recipe.ssy_Recipe_Ladapter
import com.project.rms.Recipe.ssy_Recipe_Litem
import com.project.rms.databinding.SshActivityShopBinding
import com.project.rms.databinding.SsyActivityRecipeBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.naver.Naver


class ssh_ShopActivity : AppCompatActivity() {
    //뷰바인딩
    private lateinit var binding: SshActivityShopBinding
    //쇼핑검색리스트
    var ShopList = arrayListOf<ssh_Shop_item>()
    var ShopAdapter = ssh_ShopAdapter(ShopList)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //뷰바인딩
        binding = SshActivityShopBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.shoplist.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.shoplist.adapter = ShopAdapter

        //제품을 클릭했을 때
        ShopAdapter.setItemClickListener(object: ssh_ShopAdapter.OnItemClickListener{
            override fun onClick(v: View, position: Int) {
                var link = ShopList[position].productlink //링크
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link)) //링크로 이동
                startActivity(intent)
            }
        })
        //입력을 하고 검색했을 때
        binding.shopbutton.setOnClickListener {
            GlobalScope.launch(Dispatchers.IO) {
                ShopList.clear() //원래 리스트 비우기
                search_shop(binding.shopsearch.text.toString())
            }
        }
    }
    //식재료 쇼핑 검색 함수
    fun search_shop(product:String){
        GlobalScope.launch(Dispatchers.IO) {
            val naver = Naver(clientId = "ESLGojTe7I4Uriq_7nfX", clientSecret = "sm7PyE1Nmq")
            val search_results = naver.search().shop(query = product) //product = 검색어
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                search_results.items.forEach { shop -> shop.title }
            }
            val times = search_results.items.size
            if(times==0){
                GlobalScope.launch(Dispatchers.Main) {
                    Toast.makeText(applicationContext, "검색결과가 없습니다.", Toast.LENGTH_SHORT).show()
                }
            }
            for(i in 0..times-1){
                var title = search_results.items[i].title //제품 이름
                title=title.replace("<b>","")
                title=title.replace("</b>","")
                var link = search_results.items[i].link //검색 링크
                var image = search_results.items[i].image //제품 사진
                var mall = "판매처 : "+search_results.items[i].mallName //제품 판매처
                var price = "최저가 : "+search_results.items[i].lprice+" 원" //제품 최저가
                ShopList.add(ssh_Shop_item(title, link, image,mall,price))
            }
            GlobalScope.launch(Dispatchers.Main){
                ShopAdapter.notifyDataSetChanged() //리사이클러뷰 새로고침
            }
        }
    }
}
