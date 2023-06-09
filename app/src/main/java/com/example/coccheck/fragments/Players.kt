package com.example.coccheck.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.transition.Fade
import androidx.transition.TransitionManager
import client.exceptions.CocException
import client.models.player.Player
import com.example.coccheck.R
import com.example.coccheck.activities.MainActivity
import com.example.coccheck.capitalize
import com.example.coccheck.databinding.FragmentPlayersBinding
import com.example.coccheck.hideKeyboard
import com.example.coccheck.isConnectedToInternet
import db.adapters.PlayerEntityAdapter
import db.entities.PlayerEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class Players : Fragment() {
    private lateinit var main: MainActivity
    private lateinit var binding: FragmentPlayersBinding
    private var player: Player? = null
    private lateinit var favoritePlayers: List<PlayerEntity>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        main = (activity as MainActivity)
        binding = FragmentPlayersBinding.inflate(inflater, container, false)

        getFavoritePlayers()
        initUI()
        listenToSearch()

        return binding.root
    }

    private fun getFavoritePlayers() {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                main.playerRepository.players.collect {
                    favoritePlayers = it
                }
            }
        }
    }

    private fun initUI() {
        hidePlayer()
        hideLoader()
        hideErrorToast()
    }

    private fun hidePlayer() {
        binding.playerView.player.visibility = View.GONE
    }

    private fun hideLoader() {
        binding.loader.visibility = View.GONE
    }

    private fun showLoader() {
        binding.loader.visibility = View.VISIBLE
    }

    private fun hideErrorToast() {
        binding.toastView.errorToast.visibility = View.GONE
    }

    private fun listenToSearch() {
        binding.searchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query == null) return false

                showLoader()
                Log.d("QUERY", query)
                lifecycleScope.launch {
                    getPlayer(query)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }

        })
    }

    private suspend fun getPlayer(query: String) = withContext(Dispatchers.IO) {
        if(!isConnectedToInternet(requireContext().applicationContext)) {
            return@withContext
        }

        player = try {
            val res = main.client.getPlayer(query)
            showPlayer(res)
            res
        }
        catch (e: CocException) {
            showError(e.message)
            null
        }
    }

    private suspend fun showError(message: String?) = withContext(Dispatchers.Main) {
        hideKeyboardAndLoader()
        binding.playerView.player.visibility = View.GONE

        message?.let {
            Log.d("ERROR MESSAGE", message)
            presentToast(message)
        }
    }

    private fun hideKeyboardAndLoader() {
        binding.root.hideKeyboard()
        hideLoader()
    }

    private suspend fun showPlayer(player: Player) = withContext(Dispatchers.Main) {
        hideKeyboardAndLoader()

        binding.playerView.name.text = player.name
        binding.playerView.tag.text = player.tag
        binding.playerView.townhall.text = player.townHallLevel.toString()
        binding.playerView.trophies.text = player.trophies.toString()
        binding.playerView.bestTrophies.text = player.bestTrophies.toString()
        binding.playerView.experienceLevel.text = player.expLevel.toString()
        binding.playerView.clan.text = player.clan?.name ?: resources.getString(R.string.null_field)
        binding.playerView.role.text = player.role?.capitalize() ?: resources.getString(R.string.null_field)
        binding.playerView.warStars.text = player.warStars.toString()
        binding.playerView.player.visibility = View.VISIBLE

        if (favoritePlayers.firstOrNull { it.tag == player.tag } != null) showFavoriteIcon()
        else showNotFavoriteIcon()
        listenToFavoriteIcons()
    }

    private fun showFavoriteIcon() {
        binding.playerView.favoriteIcon.visibility = View.VISIBLE
        binding.playerView.notFavoriteIcon.visibility = View.INVISIBLE
    }

    private fun showNotFavoriteIcon() {
        binding.playerView.favoriteIcon.visibility = View.INVISIBLE
        binding.playerView.notFavoriteIcon.visibility = View.VISIBLE
    }

    private fun listenToFavoriteIcons() {
        binding.playerView.notFavoriteIcon.setOnClickListener {
            lifecycleScope.launch {
                addPlayerToFavorites()
            }
        }
        binding.playerView.favoriteIcon.setOnClickListener {
            lifecycleScope.launch {
                removeClanFromFavorites()
            }
        }
    }

    private suspend fun addPlayerToFavorites() = withContext(Dispatchers.IO)  {
        val playerEntity = PlayerEntityAdapter.adapt(player!!)
        main.playerRepository.insert(playerEntity)

        withContext(Dispatchers.Main) {
            showFavoriteIcon()
        }
    }

    private suspend fun removeClanFromFavorites() = withContext(Dispatchers.IO)  {
        main.playerRepository.delete(player!!.tag)

        withContext(Dispatchers.Main) {
            showNotFavoriteIcon()
        }
    }

    private suspend fun presentToast(message: String) {
        val toastMessage: String = when(message) {
            "400" -> resources.getString(R.string.bad_request)
            "403" -> resources.getString(R.string.unauthorized_request)
            "404" -> resources.getString(
                R.string.resource_not_found,
                resources.getString(R.string.player)
            )
            "429" -> resources.getString(R.string.too_many_requests)
            "503" -> resources.getString(R.string.server_maintenance)
            "NO_CONNECTION" -> resources.getString(R.string.no_connection)
            else -> resources.getString(R.string.server_error)
        }
        binding.toastView.errorToastText.text = toastMessage
        binding.toastView.errorToast.visibility = View.VISIBLE

        delay(3000)
        fadeErrorToast()
    }

    private fun fadeErrorToast() {
        val transition = Fade()
        transition.duration = 500
        transition.addTarget(binding.toastView.errorToast)
        TransitionManager.beginDelayedTransition(binding.root, transition)
        hideErrorToast()
    }
}
