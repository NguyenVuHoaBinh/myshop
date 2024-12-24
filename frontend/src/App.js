import React, { Suspense, useEffect, useState } from 'react';
import { HashRouter, Route, Routes, Navigate } from 'react-router-dom';
import { useSelector } from 'react-redux';
import { CSpinner, useColorModes } from '@coreui/react';
import './scss/style.scss';
import './scss/examples.scss'; // Remove this in production if not needed

// Lazy-loaded Components
const DefaultLayout = React.lazy(() => import('./layout/DefaultLayout'));
const Login = React.lazy(() => import('./views/pages/login/Login'));
const Register = React.lazy(() => import('./views/pages/register/Register'));
const Page404 = React.lazy(() => import('./views/pages/page404/Page404'));
const Page500 = React.lazy(() => import('./views/pages/page500/Page500'));
const PromptBuilder = React.lazy(() => import('./views/promptbuilder/PromptBuilder'));
const AgentAction = React.lazy(() => import('./views/agentaction/AgentAction'));
const AgentBuilderPage = React.lazy(() => import('./views/agentbuilderpage/AgentBuilderPage'));
const CreateAgentPage = React.lazy(() => import('./views/createagentpage/CreateAgentPage'));
const NewPromptTemplateModal = React.lazy(() =>
  import('./views/newprompttemplatemodal/NewPromptTemplateModal')
);
const AgentBuilderModal = React.lazy(()=> import ('./views/agentbuildermodal/AgentBuilderModal'))

const App = () => {
  const { isColorModeSet, setColorMode } = useColorModes('coreui-free-react-admin-template-theme');
  const storedTheme = useSelector((state) => state.theme);

  const [isCreatingAgent, setIsCreatingAgent] = useState(false);
  const [isCreateAgentBuilderModal, setIsCreateAgentBuilderModal] = useState(false);

  useEffect(() => {
    // Handle theme based on URL parameters or stored theme
    const urlParams = new URLSearchParams(window.location.href.split('?')[1]);
    const theme = urlParams.get('theme')?.match(/^[A-Za-z0-9\s]+/)?.[0];

    if (theme) setColorMode(theme);

    if (!isColorModeSet()) setColorMode(storedTheme);
  }, [setColorMode, storedTheme, isColorModeSet]);

  return (
    <HashRouter>
      <Suspense
        fallback={
          <div className="pt-3 text-center">
            <CSpinner color="primary" variant="grow" />
          </div>
        }
      >
        <Routes>
          {/* Authentication Routes */}
          <Route exact path="/login" name="Login Page" element={<Login />} />
          <Route exact path="/register" name="Register Page" element={<Register />} />
          <Route exact path="/404" name="Page 404" element={<Page404 />} />
          <Route exact path="/500" name="Page 500" element={<Page500 />} />

          {/* Application Routes */}
          <Route exact path="/promptbuilder" name="Prompt Builder" element={<PromptBuilder />} />
          <Route
            exact
            path="/newprompttemplatemodal"
            name="New Prompt Template"
            element={<NewPromptTemplateModal />}
          />
          <Route exact path="/agentaction" name="Agent Action" element={<AgentAction />} />
          <Route
            exact
            path="/agentbuilderpage"
            name="Agent Builder Page"
            element={
              <AgentBuilderPage
                navigateToCreateAgent={() => setIsCreatingAgent(true)}
                navigateToAgentBuilderModal = {()=> setIsCreateAgentBuilderModal(true)}
              />
            }
          />
          <Route
            exact
            path="/createagent"
            name="Create Agent Page"
            element={
              isCreatingAgent ? (
                <CreateAgentPage
                  onCancel={() => setIsCreatingAgent(false)}
                  onFinish={() => setIsCreatingAgent(false)}
                />
              ) : (
                <Navigate to="/agentbuilderpage" replace />
              )
            }
          />
          <Route
            exact
            path="/createagentbuildermodal"
            name="Agent Modal Builder"
            element={
              isCreateAgentBuilderModal ? (
                <AgentBuilderModal
                  show={isCreateAgentBuilderModal}
                  onClose={() => setIsCreateAgentBuilderModal(false)}
                />
              ) : (
                <Navigate to="/agentbuilderpage" replace />
              )
            }
          />

          {/* Default Layout */}
          <Route path="*" name="Home" element={<DefaultLayout />} />
        </Routes>
      </Suspense>
    </HashRouter>
  );
};

export default App;
