package com.devrangokalp.havadurumu

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.SyncRequest
import android.content.pm.PackageManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import im.delight.android.location.SimpleLocation
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONObject
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.jar.Manifest

class MainActivity : AppCompatActivity(),AdapterView.OnItemSelectedListener {
    var tvSehiri: TextView? = null
    var location: SimpleLocation? = null
    var latitude: String? = null
    var longitude: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        var adapter=ArrayAdapter.createFromResource(this,R.array.sehirler,R.layout.spinner_tek_satir)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spnSehir.adapter=adapter

        spnSehir.setTitle("Şehir Seçiniz")
        spnSehir.setPositiveButton("seç")
        spnSehir.setOnItemSelectedListener(this)
        // location=SimpleLocation(this)

        //  oankiSehriGetir(latitude,longitude)
        spnSehir.setSelection(1) //ankara 1. positon değerinde
        VerileriGetir("Ankara")
    }
    private fun oankiSehriGetir(latitude: String?,longitude:String?) {

        var url="https://api.openweathermap.org/data/2.5/weather?lat="+latitude+"long="+longitude+"&appid=d6e6efcc2d70fefa0914c3497cc4a35c&lang=tr&units=metric"
        var sehir:String?="Şuanki yer"
        var HavadurumuRequest2=JsonObjectRequest(Request.Method.GET,url,null,object:Response.Listener<JSONObject> {

            override fun onResponse(response: JSONObject?) {

                var main=response?.getJSONObject("main")
                var sicaklik=main?.getString("temp")
                tvSicaklik.text=sicaklik.toString()

                sehir=response?.getString("name")
                //tvSehiri?.text=sehir
                tvSehiri?.setText(sehir)
                // tvKordinat.setText(longitude.toString()+" ve"+latitude.toString())


                var weather=response?.getJSONArray( "weather")
                var aciklama=weather?.getJSONObject(0)?.getString("description")
                tvAciklama.text=aciklama

                var icon=weather?.getJSONObject(0)?.getString("icon")
                if (icon?.last()=='d'){
                    rootLayout.background=getDrawable(R.drawable.bg)

                    tvAciklama.setTextColor(resources.getColor(R.color.colorAccent))
                    tvSehir.setTextColor(resources.getColor(R.color.colorAccent))
                    tvSicaklik.setTextColor(resources.getColor(R.color.colorAccent))
                    tvTarih.setTextColor(resources.getColor(R.color.colorAccent))
                    textView4.setTextColor(resources.getColor(R.color.colorAccent))
                }
                else
                {
                    rootLayout.background=getDrawable(R.drawable.gece)

                    tvAciklama.setTextColor(resources.getColor(R.color.colorAccent))
                    tvSehir.setTextColor(resources.getColor(R.color.colorAccent))
                    tvSicaklik.setTextColor(resources.getColor(R.color.colorAccent))
                    tvTarih.setTextColor(resources.getColor(R.color.colorAccent))
                    textView4.setTextColor(resources.getColor(R.color.colorAccent))
                }

                var resimDosyaAdi=resources.getIdentifier("icon_"+icon.sonKarekteriSil(),"drawable",packageName) //R.drawable.icon_50n
                imgHavaResim.setImageResource(resimDosyaAdi)
                tvTarih.text=tarihYazdir()

                //  Log.e("DEVRAN: ",sicaklik +"sehir: "+sehir +"weather: "+weather +"aciklama: "+aciklama)

            }

        },object :Response.ErrorListener{
            override fun onErrorResponse(error: VolleyError?) {
                Log.e("volley hata",""+error?.printStackTrace())
            }

        })
        /*  var request=StringRequest(Request.Method.GET,"http://www.google.com",
              object : Response.Listener<String>{
              override fun onResponse(response: String?) {
                  Toast.makeText(this@MainActivity,"cevap:"+response,Toast.LENGTH_LONG).show()

               }

          },object :Response.ErrorListener{
              override fun onErrorResponse(error: VolleyError?) {

              }

          })
  */

        MySingleton.getInstance(this)?.addToRequestQueue(HavadurumuRequest2)

    }
    override fun onNothingSelected(parent: AdapterView<*>?) {

    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

        tvSehiri = view as TextView
        if (position == 0) {

            location = SimpleLocation(this)
            if (!location!!.hasLocationEnabled()) {
                spnSehir.setSelection(1)
                Toast.makeText(this, "GPS i aç", Toast.LENGTH_LONG).show()
                SimpleLocation.openSettings(this)
            } else {
                if (ContextCompat.checkSelfPermission(
                        this,
                        android.Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                        60
                    )
                } else {
                    location = SimpleLocation(this)
                    latitude = location?.latitude.toString()  //String.format("%.6f", location?.latitude)
                    longitude =location?.longitude.toString()// String.format("%.6f", location?.longitude)
                    Log.e("LAT", "" + latitude)
                    Log.e("LONG", "" + longitude)

                    oankiSehriGetir(latitude, longitude)
                }
            }
        }
            //var oankiSehirAdi=oankiSehriGetir(latitude, longitude)
            //tvSehiri?.setText(oankiSehirAdi)
            else
        {
                var secilenSehir = parent?.getItemAtPosition(position).toString()
               // tvSehiri = view as TextView
                VerileriGetir(secilenSehir)

        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode ==60)
        {
            if (grantResults.size > 0 && grantResults[0]==PackageManager.PERMISSION_GRANTED)
            {
                location= SimpleLocation(this)
                latitude= String.format("%.6f",location?.latitude)
                longitude= String.format("%.6f",location?.longitude)
                Log.e("LAT",""+latitude)
                Log.e("LONG","" +longitude)
                oankiSehriGetir(latitude,longitude)
            }else{
                spnSehir.setSelection(1)
                Toast.makeText(this,"izin ver konum bulalım",Toast.LENGTH_LONG).show()

            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
    fun VerileriGetir(sehir:String){

        var url="https://api.openweathermap.org/data/2.5/weather?q="+sehir+"&appid=d6e6efcc2d70fefa0914c3497cc4a35c&lang=tr&units=metric"  //weather?q="+sehir+",tr&appid=
        var HavadurumuRequest=JsonObjectRequest(Request.Method.GET,url,null,object:Response.Listener<JSONObject> {
            override fun onResponse(response: JSONObject?) {

                var main=response?.getJSONObject("main")
                var sicaklik=main?.getString("temp")
                tvSicaklik.text=sicaklik

                var sehir=response?.getString("name")
                //tvSehir.text=sehir

                var weather=response?.getJSONArray( "weather")
                var aciklama=weather?.getJSONObject(0)?.getString("description")
                tvAciklama.text=aciklama

                var icon=weather?.getJSONObject(0)?.getString("icon")
                if (icon?.last()=='d'){
                    rootLayout.background=getDrawable(R.drawable.bg)

                    tvAciklama.setTextColor(resources.getColor(R.color.colorAccent))
                    tvSehir.setTextColor(resources.getColor(R.color.colorAccent))
                    tvSicaklik.setTextColor(resources.getColor(R.color.colorAccent))
                    tvTarih.setTextColor(resources.getColor(R.color.colorAccent))
                    textView4.setTextColor(resources.getColor(R.color.colorAccent))
                }
                else
                {
                    rootLayout.background=getDrawable(R.drawable.gece)

                    tvAciklama.setTextColor(resources.getColor(R.color.colorAccent))
                    tvSehir.setTextColor(resources.getColor(R.color.colorAccent))
                    tvSicaklik.setTextColor(resources.getColor(R.color.colorAccent))
                    tvTarih.setTextColor(resources.getColor(R.color.colorAccent))
                    textView4.setTextColor(resources.getColor(R.color.colorAccent))
                }

                var resimDosyaAdi=resources.getIdentifier("icon_"+icon.sonKarekteriSil(),"drawable",packageName) //R.drawable.icon_50n
                imgHavaResim.setImageResource(resimDosyaAdi)
                tvTarih.text=tarihYazdir()

                //  Log.e("DEVRAN: ",sicaklik +"sehir: "+sehir +"weather: "+weather +"aciklama: "+aciklama)
            }

        },object :Response.ErrorListener{
            override fun onErrorResponse(error: VolleyError?) {

              //  Log.e("volley hata",error)
            }

        })
        /*  var request=StringRequest(Request.Method.GET,"http://www.google.com",
              object : Response.Listener<String>{
              override fun onResponse(response: String?) {
                  Toast.makeText(this@MainActivity,"cevap:"+response,Toast.LENGTH_LONG).show()

               }

          },object :Response.ErrorListener{
              override fun onErrorResponse(error: VolleyError?) {

              }

          })
  */

        MySingleton.getInstance(this)?.addToRequestQueue(HavadurumuRequest)

    }
    fun tarihYazdir():String{

        var takvim=Calendar.getInstance().time
        var formatlayici=SimpleDateFormat("EEEE,MMMM yyyy", Locale("tr"))
        var tarih=formatlayici.format(takvim)

        return tarih

    }


}

private fun String?.sonKarekteriSil(): String? {
    return this?.substring(0,this.length-1)
}


