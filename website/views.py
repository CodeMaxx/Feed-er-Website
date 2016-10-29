from django.shortcuts import render, reverse, get_object_or_404
from django.http import HttpResponse, HttpResponseRedirect
from django.contrib.auth.decorators import login_required
from django.contrib.auth.models import User
from django.contrib.auth import logout, authenticate, login
from .models import *
from django.utils.dateparse import parse_date
from datetime import datetime
from dateutil.parser import parse
from django.utils import timezone

# Create your views here.


## Show the main view
## More like index page
def signin(request):
    if request.method == "GET":
        user = request.user
        if user.is_authenticated():
            return HttpResponseRedirect(reverse('website:home'))

        return render(request, "signin.html")

    elif request.method == "POST":

        username = request.POST['username']
        password = request.POST['password']

        user = authenticate(username=username, password=password)
        if user is None:
            return HttpResponse("Invalid Username/Password")
        member = user.member
        if member.mtype == 'ST':
            return HttpResponse("Invalid Username/Password")
        login(request, user)
        return HttpResponseRedirect(reverse('website:home'))


## Signup view which takes data and creates the account
def signup(request):

    if request.method == "GET":
        return HttpResponseRedirect(reverse('website:signin'))
    elif request.method == "POST":
        try:
            fullname = request.POST['fullname']
            username = request.POST['sUsername']
            password = request.POST['sPassword']
            isProf = request.POST.get('prof')
        except:
            return HttpResponse(request)

        ## We have the data
        userexists = User.objects.filter(username=username)
        if len(userexists) != 0:
            return HttpResponse(
                "Email already used. Please used a different email.")

        newuser = User.objects.create(username=username, email=username)
        newuser.set_password(password)
        newuser.save()

        if isProf == "on":
            mtype = "PR"
        else:
            mtype = "TA"

        newmember = Member.objects.create(
            user=newuser,
            fullname=fullname,
            mtype=mtype, )
        newmember.save()
        return HttpResponseRedirect(reverse('website:signin'))


## Add views for Home Page
## Will contain different stuff for Admins and TAs/Profs
@login_required
def home(request):
    ## Anon users are shooed away
    if request.user.is_anonymous():
        return HttpResponseRedirect(reverse('website:signin'))

    if request.user.is_superuser:
        return HttpResponseRedirect('/admin')

    user = request.user
    member = get_object_or_404(Member, user=user)

    context = {'member': member}

    return render(request, 'home.html', context)


## Signout and return to homepage
def signout(request):
    logout(request)
    return HttpResponseRedirect(reverse('website:signin'))


## Complete Registration
## Completes facebook signup
def completeReg(request):
    if request.method == "GET":  # the user is registered now
        user = request.user

        if not user.is_authenticated():
            return HttpResponse('Invalid Facebook redirect!')

        context = {
            'fullname': user.first_name + ' ' + user.last_name,
            'username': user.username,
        }
        return render(request, 'complete-reg.html', context)

    elif request.method == "POST":

        try:
            fullname = request.POST['fullname']
            username = request.POST['username']
            isProf = request.POST.get('prof')
        except:
            return HttpResponse("Invalid fields!")

        user = User.objects.get(username=username)

        if isProf == "on":
            mtype = "PR"
        else:
            mtype = "TA"

        member = Member.objects.filter(user=user)
        if len(member) != 0:
            return HttpResponse('This user already exists! Try logging in!')

        member = Member.objects.create(
            user=user, fullname=fullname, mtype=mtype)
        member.save()
        return HttpResponseRedirect(reverse('website:home'))


######################################################################################s
#     Course page
#    View courses, add courses and show details for courses 
### View courses, all courses for admins, Selected courses for Profs, or TAs
######################################################################################
@login_required
def view_courses(request):

    member = Member.objects.get(user=request.user)

    ## Admin, bring up all the courses
    if member.mtype == "AD":
        all_courses = Course.objects.all()
        if (len(all_courses) == 0):
            return render(request, 'view_courses.html',
                          {'error': 'No courses to view.'})
        try:
            profs = [
                course.members.filter(mtype="PR")[0] for course in all_courses
            ]
        except:
            return render(request, 'view_courses.html',
                          {'error': 'No courses to view.'})

        return render(request, 'view_courses.html', {
            'all_courses': all_courses,
            'member': member,
            'profs': profs
        })
    ## Prof or TA, bring up the courses of the prof or TA
    else:
        all_courses = member.course_set.all()
        return render(request, 'view_courses.html',
                      {'all_courses': all_courses,
                       'member': member})


### Add courses for Admins
@login_required
def add_courses(request):
    member = Member.objects.get(user=request.user)
    if request.method == "GET":
        profs = Member.objects.filter(mtype="PR")
        tas = Member.objects.filter(mtype="TA")
        students = Member.objects.filter(mtype="ST")

        context = {
            'mtype': member.mtype,
            'profs': profs,
            'students': students,
            'tas': tas,
        }

        return render(request, 'add_courses.html', context)

    elif request.method == "POST":

        print(request.POST)

        try:
            course_name = request.POST['name']
            code = request.POST['code']
            semester = int(request.POST['semester'])
        except:
            return HttpResponse(request)

        course = Course.objects.create(
            name=course_name, course_code=code, semester=semester)

        prof = request.POST.get('prof')
        students = request.POST.getlist('student')
        tas = request.POST.getlist('ta')
        print(prof, students, tas)

        prof = Member.objects.get(user__username=prof)
        course.members.add(prof)

        for ta in tas:
            taObj = Member.objects.get(user__username=ta)
            course.members.add(taObj)

        for student in students:
            stuObj = Member.objects.get(user__username=student)
            course.members.add(stuObj)

        course.save()

        ## Save the midsem Feedback now
        ## Copied from add_feedback, remember to change this in case of bugs
        ##########################################################################################################################

        deadlineM = parse(request.POST['deadlineM'])

        myfeedback = Feedback.objects.create(
            name=request.POST['feedbacknameM'],
            course=course,
            deadline=deadlineM)
        myfeedback.save()

        ## Get the rating questions
        rating_q = request.POST.getlist('RquestionM')
        for r in rating_q:
            newRatingQuestion = FeedbackQuestion.objects.create(
                question=r,
                feedback=myfeedback,
                q_type="RATE", )
            newRatingQuestion.save()

            # Give the rating answers, 5 instances 
            for i in range(5):
                rating = FeedbackRatingAnswer.objects.create(
                    q=newRatingQuestion,
                    rating=(i + 1), )
                rating.save()

        ## Get the Short Questions
        short_q = request.POST.getlist('SquestionM')
        if short_q is not None:
            for question in short_q:
                shortQuestion = FeedbackQuestion.objects.create(
                    question=question,
                    feedback=myfeedback,
                    q_type='SHORT', )
                shortQuestion.save()

        ## Get the MCQ questions
        ## Tricky
        mcq_question = request.POST.getlist('MquestionM')
        if mcq_question is not None:
            counter = 0  # Counter for m_answer
            q_no = 0  # Number of questions
            MAnswer = request.POST.getlist('MAnswerM')
            NoOptions = request.POST.getlist('NoOptionsM')

            ## Add question and add options
            for question in mcq_question:
                m_q = FeedbackQuestion.objects.create(
                    question=question,
                    feedback=myfeedback,
                    q_type='MCQ', )
                m_q.save()

                ## Add options
                options = int(NoOptions[q_no])
                for i in range(options):
                    addOption = FeedbackMCQChoice.objects.create(
                        text=MAnswer[counter],
                        q=m_q, )
                    addOption.save()
                    counter += 1

                q_no += 1

        ########################################################################################################################## 

        ## Save the midsem Feedback now
        ## Copied from add_feedback, remember to change this in case of bugs
        ##########################################################################################################################

        deadlineE = parse(request.POST['deadlineE'])

        myfeedback = Feedback.objects.create(
            name=request.POST['feedbacknameE'],
            course=course,
            deadline=deadlineE)
        myfeedback.save()

        ## Get the rating questions
        rating_q = request.POST.getlist('RquestionE')
        for r in rating_q:
            newRatingQuestion = FeedbackQuestion.objects.create(
                question=r,
                feedback=myfeedback,
                q_type="RATE", )
            newRatingQuestion.save()

            # Give the rating answers, 5 instances 
            for i in range(5):
                rating = FeedbackRatingAnswer.objects.create(
                    q=newRatingQuestion,
                    rating=(i + 1), )
                rating.save()

        ## Get the Short Questions
        short_q = request.POST.getlist('SquestionE')
        if short_q is not None:
            for question in short_q:
                shortQuestion = FeedbackQuestion.objects.create(
                    question=question,
                    feedback=myfeedback,
                    q_type='SHORT', )
                shortQuestion.save()

        ## Get the MCQ questions
        ## Tricky
        mcq_question = request.POST.getlist('MquestionE')
        if mcq_question is not None:
            counter = 0  # Counter for m_answer
            q_no = 0  # Number of questions
            MAnswer = request.POST.getlist('MAnswerE')
            NoOptions = request.POST.getlist('NoOptionsE')

            ## Add question and add options
            for question in mcq_question:
                m_q = FeedbackQuestion.objects.create(
                    question=question,
                    feedback=myfeedback,
                    q_type='MCQ', )
                m_q.save()

                ## Add options
                options = int(NoOptions[q_no])
                for i in range(options):
                    addOption = FeedbackMCQChoice.objects.create(
                        text=MAnswer[counter],
                        q=m_q, )
                    addOption.save()
                    counter += 1

                q_no += 1

        ########################################################################################################################## 

        return HttpResponseRedirect(reverse("website:view_course"))


@login_required
def course_detail(request, pk):

    member = Member.objects.get(user=request.user)
    course = get_object_or_404(Course, pk=pk)

    if member.mtype == "AD":
        feedbacks = course.feedback_set.all()
        assignments = course.assignment_set.all()
        courseprof = course.members.filter(mtype="PR")[0]

    else:
        if course not in member.course_set.all():
            return render(request, 'course_detail.html', {
                'error':
                "Sorry, this is not your course, you aren't allowed to see it."
            })

        feedbacks = course.feedback_set.all()
        assignments = course.assignment_set.all()
        courseprof = None

    context = {
        'feedbacks': feedbacks,
        'member': member,
        'course': course,
        'assignments': assignments,
        'courseprof': courseprof,
        'students_enrolled': len(course.members.filter(mtype="ST")),
        'ta_count': len(course.members.filter(mtype="TA")),
        'now':datetime.now(),
    }

    return render(request, 'course_detail.html', context)


#################################################################################################
#     Feedback pages - Add feedback, view feedback replies
#     Anything else? 
#################################################################################################
@login_required
def add_feedback(request, course_id):

    member = Member.objects.get(user=request.user)

    if member.mtype == "AD":
        return render(
            request, 'add_feedback.html',
            {'error': 'Professors and TAs will now handle feedbacks.'})

    else:
        # GET request, render stuff here
        if request.method == "GET":
            course = get_object_or_404(Course, pk=course_id)
            if course not in member.course_set.all():
                return render(request, 'add_feedback.html', {
                    'error':
                    'This is not your course. You cannot add a feedback form for the same.'
                })

            context = {
                "course": course,
                "member": member,
            }
            return render(request, 'add_feedback.html', context)

        # POST request sent, add the feedback here
        elif request.method == "POST":
            print(request.POST)
            # return HttpResponse("Not added")

            course = Course.objects.get(pk=int(request.POST['course_id']))
            deadline = parse(request.POST['deadline'])

            myfeedback = Feedback.objects.create(
                name=request.POST['feedbackname'],
                course=course,
                deadline=deadline)
            myfeedback.save()

            ## Get the rating questions
            rating_q = request.POST.getlist('Rquestion')

            for r in rating_q:
                newRatingQuestion = FeedbackQuestion.objects.create(
                    question=r,
                    feedback=myfeedback,
                    q_type="RATE", )
                newRatingQuestion.save()

                # Give the rating answers, 5 instances 
                for i in range(5):
                    rating = FeedbackRatingAnswer.objects.create(
                        q=newRatingQuestion,
                        rating=(i + 1), )
                    rating.save()

            ## Get the Short Questions
            short_q = request.POST.getlist('Squestion')
            if short_q is not None:
                for question in short_q:
                    shortQuestion = FeedbackQuestion.objects.create(
                        question=question,
                        feedback=myfeedback,
                        q_type='SHORT', )
                    shortQuestion.save()

            ## Get the MCQ questions
            ## Tricky
            mcq_question = request.POST.getlist('Mquestion')
            if mcq_question is not None:
                counter = 0  # Counter for m_answer
                q_no = 0  # Number of questions
                MAnswer = request.POST.getlist('MAnswer')
                NoOptions = request.POST.getlist('NoOptions')

                ## Add question and add options
                for question in mcq_question:
                    m_q = FeedbackQuestion.objects.create(
                        question=question,
                        feedback=myfeedback,
                        q_type='MCQ', )
                    m_q.save()

                    ## Add options
                    options = int(NoOptions[q_no])
                    for i in range(options):
                        addOption = FeedbackMCQChoice.objects.create(
                            text=MAnswer[counter],
                            q=m_q, )
                        addOption.save()
                        counter += 1

                    q_no += 1

            return HttpResponseRedirect(reverse('website:home'))

        else:
            return HttpResponse("Bad request!")


@login_required
def view_feedback(request, pk):

    member = Member.objects.get(user=request.user)

    if member.mtype == "AD":
        return render(
            request, 'add_feedback.html',
            {'error': 'Professors and TAs will now handle feedbacks.'})

    else:
        # GET request, render stuff here
        if request.method == "GET":
            feedback = get_object_or_404(Feedback, pk=pk)
            course = feedback.course

            if course not in member.course_set.all():
                return render(request, 'view_feedback.html',
                              {'error': 'This is not your course.'})

            rating_q = feedback.feedbackquestion_set.filter(q_type="RATE")
            short_q = feedback.feedbackquestion_set.filter(q_type="SHORT")
            mcq_q = feedback.feedbackquestion_set.filter(q_type="MCQ")

            rating = []
            for q in rating_q:
                rating.append({
                    'question': q,
                    'answers': q.feedbackratinganswer_set.all(),
                })

            short = []
            if len(short_q) != 0:
                for q in short_q:
                    short.append({
                        'question': q,
                        'answers': q.feedbackshortanswer_set.all(),
                    })

            mcq = []
            if len(mcq_q) != 0:
                for q in mcq_q:
                    mcq.append({
                        'question': q,
                        'answers': q.feedbackmcqchoice_set.all(),
                    })

            context = {
                'feedback': feedback,
                'course': course,
                'rating': rating,
                'short': short,
                'mcq': mcq,
                'now': datetime.now(),
            }

            return render(request, 'view_feedback.html', context)


### Add feedbacks considering all courses.
@login_required
def add_feedback_all(request):
    member = Member.objects.get(user=request.user)

    if member.mtype == "AD":
        return render(
            request, 'add_feedback_all.html',
            {'error': 'Professors and TAs will now handle feedbacks.'})

    else:
        # GET request, render stuff here
        if request.method == "GET":

            all_courses = member.course_set.all()
            if len(all_courses) == 0:
                return render(request, 'add_feedback_all.html',
                              {'error': 'You have no courses right now.'})

            context = {
                'all_courses': all_courses,
                'member': member,
            }

            return render(request, 'add_feedback_all.html', context)

        else:
            return HttpResponseRedirect(reverse('website:home'))


### View all feedbacks by courses!
@login_required
def view_feedback_all(request):

    member = Member.objects.get(user=request.user)

    if member.mtype == "AD":
        return render(
            request, 'view_feedback_all.html',
            {'error': 'Professors and TAs will now handle feedbacks.'})

    else:
        # GET request, render stuff here
        if request.method == "GET":

            all_courses = member.course_set.all()
            if len(all_courses) == 0:
                return render(request, 'view_feedback_all.html',
                              {'error': 'You have no courses right now.'})

            course_data = []
            for course in all_courses:
                course_data.append({
                    'course': course,
                    'feedbacks': course.feedback_set.all(),
                })

            context = {
                'course_data': course_data,
                'member': member,
            }

            return render(request, 'view_feedback_all.html', context)

        else:
            return HttpResponseRedirect(reverse('website:home'))


########################################################################################################
#### Assignments


## Display all assignments of all courses of the prof/TA
@login_required()
def assigns(request):
    member = Member.objects.get(user=request.user)

    if member.mtype == "AD":
        return render(request, 'view_assignments.html', {
            "error":
            "Trespassers will be shot. Survivors will be shot again :P"
        })

    all_courses = member.course_set.all()
    assignments = []  # 2D array ; each element contains all assignments of a subject

    single_course_assign = []

    for course in all_courses:
        assign_for_course = Assignment.objects.filter(course=course)
        for assign in assign_for_course:
            single_course_assign.append(assign)
        assignments.append(single_course_assign)
        single_course_assign = []

    context = {'courses': all_courses, 'assigns': assignments}
    return render(request, 'view_assignments.html', context)


## Allows prof and TAs to add assignments
@login_required()
def add_assigns(request):
    if request.method == "GET":
        member = Member.objects.get(user=request.user)

        if member.mtype == "AD":
            return render(
                request, 'add_assignments.html',
                {'error': 'Professors and TAs handle course assignments.'})

        all_courses = member.course_set.all()

        if len(all_courses) == 0:
            return render(request, 'add_assignments.html',
                          {'error': 'You are not taking any course.'})

        return render(request, 'add_assignments.html',
                      {'courses': all_courses})

    elif request.method == "POST":
        member = Member.objects.get(user=request.user)

        if member.mtype == "AD":
            return render(
                request, 'add_assignments.html',
                {'error': 'Professors and TAs handle course assignments.'})

        name = request.POST['name']
        desc = request.POST.get('desc')
        deadline = parse(request.POST['deadline'])
        course_code = request.POST['course']
        course = Course.objects.get(course_code=course_code)

        assignment = Assignment.objects.create(
            name=name, description=desc, deadline=deadline, course=course)
        assignment.save()

        return HttpResponseRedirect(reverse('website:view_assignment'))


## View informations of a specific assingment
@login_required
def view_assign(request, pk):
    user = request.user
    member = Member.objects.get(user=user)

    if member.mtype == "AD":
        return render(request, 'view_assignment_info.html', {
            "error":
            "Trespassers will be shot. Survivors will be shot again :P"
        })

    if request.method == "GET":
        assign = Assignment.objects.get(pk=pk)
        course = assign.course
        member_list = course.members.all()
        if member in member_list:
            return render(request, 'view_assignment_info.html',
                          {'assign': assign})
        
        else:
            return render(request, 'view_assignment_info.html',
                          {'error': "Sorry you are not taking this course."})


########################################################################################################
##################### Website Ends Here. Mobile API begins
