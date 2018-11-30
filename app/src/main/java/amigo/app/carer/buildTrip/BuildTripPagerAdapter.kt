package amigo.app

import amigo.app.auth.User
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.view.View
import android.view.ViewGroup

class BuildTripPagerAdapter(fragmentManager: FragmentManager, val userName: String, val userID: String) : FragmentPagerAdapter(fragmentManager) {
    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        // do nothing
    }

    private val fragments = LinkedHashMap<Int, Fragment>()


    override fun getItem(position: Int): Fragment {
        return fragments[position] ?: when (position) {
            0 -> {
                fragments[position] = LocationFragment.newInstance();
                fragments[position]!!
            }
            1 -> {
                fragments[position] = DestinationFragment.newInstance();
                fragments[position]!!
            }
            2 -> {
                fragments[position] = TransportFragment.newInstance();
                fragments[position]!!
            }
            else -> LocationFragment.newInstance()
        }
    }

    override fun getCount(): Int {
        return PagesModel.values().size
    }

    override fun getPageTitle(position: Int): CharSequence {
        return PagesModel.fromInt(position).title
    }
}