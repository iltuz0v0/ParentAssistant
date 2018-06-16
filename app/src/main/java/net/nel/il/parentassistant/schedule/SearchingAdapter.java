package net.nel.il.parentassistant.schedule;

import android.app.Activity;
import android.content.Context;
import android.icu.text.IDNA;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import net.nel.il.parentassistant.R;
import net.nel.il.parentassistant.ToastManager;
import net.nel.il.parentassistant.main.InfoWindow;
import net.nel.il.parentassistant.model.InfoAccount;
import net.nel.il.parentassistant.model.OutputAccount;

import java.util.ArrayList;
import java.util.List;

public class SearchingAdapter extends RecyclerView.Adapter<SearchingViewHolder>
        implements View.OnClickListener{

    private List<Account> accounts;

    private InfoWindow infoWindow = new InfoWindow();

    private SearchingAdapterCallback searchingAdapterCallback;

    public interface SearchingAdapterCallback{
        Context getAppContext();
        Activity getContext();
    }

    SearchingAdapter(OutputAccount outputAccount, SearchFragment searchFragment){
        accounts = new ArrayList<>();
        searchingAdapterCallback = (SearchingAdapterCallback) searchFragment;
        setData(outputAccount);
    }

    @Override
    public SearchingViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(searchingAdapterCallback.getAppContext())
                .inflate(R.layout.searching_element, parent, false);
        return new SearchingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(SearchingViewHolder holder, int position) {
        holder.fromTextView.setText(accounts.get(position).from);
        holder.toTextView.setText(accounts.get(position).to);
        holder.place.setTag(position);
        holder.account.setTag(position);
        holder.requestSending.setTag(position);
        holder.place.setOnClickListener(this);
        holder.account.setOnClickListener(this);
        holder.requestSending.setOnClickListener(this);
    }

    @Override
    public int getItemCount() {
        return accounts.size();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.look_in_account:
                lookInAccount(v);
                break;
            case R.id.look_in_place:
                lookInPlace(v);
                break;
        }
    }

    private void lookInPlace(View v){
        SubMapFragment subMapFragment = new SubMapFragment();
        subMapFragment.setPoints(accounts.get((int)v.getTag()).pointOne,
                accounts.get((int)v.getTag()).pointTwo);
        searchingAdapterCallback.getContext().
                getFragmentManager().beginTransaction()
                .add(R.id.main_frame, subMapFragment)
                .addToBackStack(null).commit();
    }

    private void lookInAccount(View v){
        infoWindow.createCompanionRequestWindow(
                searchingAdapterCallback.getContext(),
                accounts.get((int)v.getTag()).infoAccounts);
    }

    public void setData(OutputAccount outputAccount){
        if(outputAccount.getPeopleIdentifiers() == null){
            return;
        }
        if(outputAccount.getPeopleIdentifiers().size() == 0){
            noInformationToast();
            return;
        }
        this.accounts.clear();
        this.accounts.addAll(getAccountsFromOutputAccount(outputAccount));
        notifyDataSetChanged();
    }

    private List<Account> getAccountsFromOutputAccount(OutputAccount outputAccount){
        List<Account> accounts = new ArrayList<>();
        Account account = new Account();
        int index = outputAccount.getPeopleIdentifiers().get(0);
        List<InfoAccount> infoAccounts = new ArrayList<>();
        for(int element = 0; element < outputAccount.getNames().size(); element++){
            if(outputAccount.getPeopleIdentifiers().get(element) != index){
                account.infoAccounts = infoAccounts;
                accounts.add(account);
                account = new Account();
                infoAccounts = new ArrayList<>();
                fillFullAccountInformation(account, outputAccount, element, infoAccounts);
            }
            else{
                fillFullAccountInformation(account, outputAccount, element, infoAccounts);
            }
            if(element == outputAccount.getNames().size() - 1){
                account.infoAccounts = infoAccounts;
                accounts.add(account);
                account = new Account();
                infoAccounts = new ArrayList<>();
            }
            index = outputAccount.getPeopleIdentifiers().get(element);

        }
        return accounts;
    }

    private void noInformationToast(){
        ToastManager.showToast(searchingAdapterCallback.getAppContext()
                .getString(R.string.toast_no_elements),
                searchingAdapterCallback.getAppContext());
    }

    private void fillFullAccountInformation(Account account,
            OutputAccount outputAccount, int element, List<InfoAccount> infoAccounts){
        fillAccount(account, outputAccount, element);
        InfoAccount infoAccount = new InfoAccount();
        fillInfoAccount(infoAccount, outputAccount, element);
        infoAccounts.add(infoAccount);
    }

    private void fillInfoAccount(InfoAccount infoAccount,
                                 OutputAccount outputAccount, int element){
        infoAccount.setName(outputAccount.getName().get(element));
        infoAccount.setAge(outputAccount.getAge().get(element));
        infoAccount.setHobby(outputAccount.getHobbies().get(element));
        infoAccount.setPhoto(outputAccount.getPhotos().get(element));
    }

    private void fillAccount(Account account, OutputAccount outputAccount,
                             int element){
        account.from = outputAccount.getFrom().get(element);
        account.to = outputAccount.getTo().get(element);
        account.pointOne = new LatLng(outputAccount.getPointOneLat().get(element),
                outputAccount.getPointOneLng().get(element));
        account.pointTwo = new LatLng(outputAccount.getPointTwoLat().get(element),
                outputAccount.getPointTwoLng().get(element));
    }


}
