from django.shortcuts import render,reverse
from django.http import HttpResponse, HttpResponseRedirect
from django.contrib.auth.decorators import login_required
from django.contrib.auth.models import User
from django.contrib.auth import logout, authenticate, login
from .models import *
# Create your views here.

## Show the main view
def signin(request):
	if request.method == "GET":
		user = request.user
		if user.is_authenticated():
			return HttpResponseRedirect('home')

		return render(request, "signin.html")

		
	elif request.method == "POST":

		username = request.POST['username']
		password = request.POST['password']

		user = authenticate(username=username, password=password)
		login(request, user)
		return HttpResponseRedirect('home')



## Signup view which takes data and creates the account
def signup(request):

	if request.method == "GET":
		return HttpResponseRedirect('signin')
	elif request.method == "POST":
		try:
			fullname = request.POST['fullname']
			username = request.POST['sUsername']
			password = request.POST['sPassword']
			isProf = request.POST['prof']
		except:
			return HttpResponseRedirect('signin')

		## We have the data
		userexists = User.objects.filter(username=username)
		if len(userexists) != 0:
			return HttpResponseRedirect('signin')

		newuser = User.objects.create(username=username, password=password)
		newuser.save()
		mtype = "PR" if isProf==True else "TA"
		
		newmember = Member.objects.create(
			user=newuser,
			fullname=fullname,
			mtype=mtype,
		)
		newmember.save()

		return HttpResponseRedirect('home')


@login_required
def home(request):
	## Anon users are shooed away
	if request.user.is_anonymous():
		return HttpResponseRedirect('signin')

	## get member and log out if student
	user = User.objects.get(username=request.user.username)
	member = Member.objects.get(user=user)

	if member.mtype == "ST":
		logout(request)
		return HttpResponse('You are not allowed to visit this page. Please use the app since you are a student.')

	return HttpResponse("Home page!")

def completeReg(request):

	if request.method == "GET":	# the user is registered now
		user = request.user

		if not user.is_authenticated():
			return HttpResponse('Invalid Facebook redirect!')

		context = {
			'fullname':user.first_name + ' ' + user.last_name,
			'username':user.username,
		}
		return render(request,'complete-reg.html',context)

	elif request.method == "POST":
		
		try:
			fullname = request.POST.get('fullname')
			username = request.POST.get('username')
			isProf = request.POST.get('prof')
			print(fullname,username,isProf)
		except:
			return HttpResponse('Invalid POST request')

		user = User.objects.get(username=username)

		mtype = "PR" if isProf else "TA"
		member = Member.objects.create(user=user,fullname=fullname,mtype=mtype)
		member.save()
		return HttpResponseRedirect('')

	else:
		return HttpResponseRedirect('')

def courses(request):
	return HttpResponse('Sign in page!')

def add_courses(request):
	return HttpResponse('Sign in page!')

def assigns(request):
	return HttpResponse('Sign in page!')

def add_assigns(request):
	return HttpResponse('Sign in page!')

def course_detail(request,pk,year,sem):
	return HttpResponse('Sign in page!')
